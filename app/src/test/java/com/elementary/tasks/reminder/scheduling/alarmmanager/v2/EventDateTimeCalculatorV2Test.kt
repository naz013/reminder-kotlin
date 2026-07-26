package com.elementary.tasks.reminder.scheduling.alarmmanager.v2

import com.elementary.tasks.BaseTest
import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.minusMillis
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class EventDateTimeCalculatorV2Test : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var strategyResolver: BehaviorStrategyResolverV2
  private lateinit var calculator: EventDateTimeCalculatorV2

  private val now: LocalDateTime = LocalDateTime.of(2025, 1, 6, 10, 0)

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    strategyResolver = mockk()
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } answers {
      (firstArg<LocalDateTime>()).toEpochSecond(org.threeten.bp.ZoneOffset.UTC) * 1000
    }
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
  fun `returns null when the calculated time is in the past`() {
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } returns 0L

    val result = calculator.calculateEventDateTime(reminderV2(eventDateTime = now.plusHours(1)))

    assertNull(result)
  }
}
