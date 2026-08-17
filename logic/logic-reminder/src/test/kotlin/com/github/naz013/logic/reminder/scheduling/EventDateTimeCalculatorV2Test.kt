package com.github.naz013.logic.reminder.scheduling

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.minusMillis
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class EventDateTimeCalculatorV2Test {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var strategyResolver: BehaviorStrategyResolverV2
  private lateinit var calculator: EventDateTimeCalculatorV2

  private val now: LocalDateTime = LocalDateTime.of(2025, 1, 6, 10, 0)

  @Before
  fun setUp() {
    dateTimeManager = mockk()
    strategyResolver = mockk()
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } answers {
      (firstArg<LocalDateTime>()).toEpochSecond(org.threeten.bp.ZoneOffset.UTC) * 1000
    }
    every { dateTimeManager.getCurrentDateTime() } returns now
    every { dateTimeManager.logDateTime(any()) } returns ""
    every { strategyResolver.resolve(any()) } returns mockk(relaxed = true)
    calculator = EventDateTimeCalculatorV2(strategyResolver, dateTimeManager)
  }

  private fun reminderV2(eventDateTime: LocalDateTime?, remindBefore: Long? = null) =
    ReminderV2(
      recurrence = RecurrenceRule.Once,
      schedule = ReminderSchedule(startDateTime = now, eventDateTime = eventDateTime),
      notification = NotificationSettingsOverride(remindBefore = remindBefore),
    )

  @Test
  fun `returns null when there is no event time`() {
    val result = calculator.calculateEventDateTime(reminderV2(eventDateTime = null))

    assertNull(result)
  }

  @Test
  fun `returns a positive millis value for a future event time`() {
    val future = now.plusHours(1)

    val result = calculator.calculateEventDateTime(reminderV2(eventDateTime = future))

    assertEquals(dateTimeManager.toMillis(future.withSecond(0)), result)
  }

  @Test
  fun `applies the remindBefore adjustment`() {
    val future = now.plusHours(1)
    val remindBeforeMillis = 10 * 60 * 1000L

    val result = calculator.calculateEventDateTime(reminderV2(eventDateTime = future, remindBefore = remindBeforeMillis))

    val expected = future.minusMillis(remindBeforeMillis).withSecond(0)
    assertEquals(dateTimeManager.toMillis(expected), result)
  }

  @Test
  fun `returns null when the calculated time is genuinely in the past`() {
    val past = now.minusDays(1)

    val result = calculator.calculateEventDateTime(reminderV2(eventDateTime = past))

    assertNull(result)
  }

  @Test
  fun `tolerates a due time earlier in the current minute after seconds are zeroed`() {
    every { dateTimeManager.getCurrentDateTime() } returns now.plusSeconds(45)

    val result = calculator.calculateEventDateTime(reminderV2(eventDateTime = now))

    assertEquals(dateTimeManager.toMillis(now), result)
  }
}
