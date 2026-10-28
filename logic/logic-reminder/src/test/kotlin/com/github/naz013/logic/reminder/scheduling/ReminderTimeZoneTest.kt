package com.github.naz013.logic.reminder.scheduling

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.IntervalRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.MonthlyRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.ReminderBehaviorStrategyV2
import com.github.naz013.logic.reminder.behavior.SimpleDateStrategyV2
import com.github.naz013.logic.reminder.behavior.WeekdayRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.YearlyRepeatStrategyV2
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.SkipReminderUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import java.util.TimeZone

/**
 * Device zone is America/New_York; fixed reminders use Europe/Berlin. "Now" is 2025-01-06 05:00 in New York,
 * i.e. 10:00Z and 11:00 in Berlin.
 */
class ReminderTimeZoneTest {
  private val originalZone: TimeZone = TimeZone.getDefault()
  private val nowProvider = mockk<NowDateTimeProvider>()
  private val calculator = mockk<RecurrenceCalculator>()
  private lateinit var manager: DateTimeManager

  private val eventUtc = LocalDateTime.of(2025, 1, 6, 8, 0)

  @Before
  fun setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
    every { nowProvider.nowDateTime() } returns LocalDateTime.of(2025, 1, 6, 5, 0)
    every { nowProvider.nowInstant() } returns Instant.parse("2025-01-06T10:00:00Z")
    manager = DateTimeManager(nowProvider, mockk(relaxed = true))
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(originalZone)
  }

  private fun reminder(
    zoneId: String?,
    recurrence: RecurrenceRule = RecurrenceRule.Once,
    eventDateTime: LocalDateTime? = eventUtc,
  ) = ReminderV2(
    recurrence = recurrence,
    timeZoneId = zoneId,
    schedule = ReminderSchedule(startDateTime = eventUtc, eventDateTime = eventDateTime),
  )

  private fun millisOf(utc: LocalDateTime) = utc.toEpochSecond(ZoneOffset.UTC) * 1000

  private fun eventCalculator(): EventDateTimeCalculatorV2 {
    val resolver = mockk<BehaviorStrategyResolverV2>()
    every { resolver.resolve(any()) } returns mockk<ReminderBehaviorStrategyV2>(relaxed = true)
    return EventDateTimeCalculatorV2(resolver, manager)
  }

  @Test
  fun `weekday strategy evaluates a fixed reminder in its own zone`() {
    val event = slot<LocalDateTime>()
    val after = slot<LocalDateTime>()
    every { calculator.findNextDayOfWeekDateTime(capture(event), any(), capture(after)) } returns eventUtc
    val strategy = WeekdayRepeatStrategyV2(manager, calculator)

    strategy.calculateNextOccurrence(reminder("Europe/Berlin", RecurrenceRule.Weekly(listOf(1))), eventUtc)

    assertEquals(LocalDateTime.of(2025, 1, 6, 9, 0), event.captured)
    assertEquals(LocalDateTime.of(2025, 1, 6, 11, 0), after.captured)
  }

  @Test
  fun `weekday strategy evaluates a floating reminder in the device zone`() {
    val event = slot<LocalDateTime>()
    val after = slot<LocalDateTime>()
    every { calculator.findNextDayOfWeekDateTime(capture(event), any(), capture(after)) } returns eventUtc
    val strategy = WeekdayRepeatStrategyV2(manager, calculator)

    strategy.calculateNextOccurrence(reminder(null, RecurrenceRule.Weekly(listOf(1))), eventUtc)

    assertEquals(manager.utcToLocal(eventUtc), event.captured)
    assertEquals(LocalDateTime.of(2025, 1, 6, 3, 0), event.captured)
    assertEquals(manager.getCurrentDateTime(), after.captured)
  }

  @Test
  fun `monthly strategy evaluates a fixed reminder in its own zone`() {
    val event = slot<LocalDateTime>()
    val after = slot<LocalDateTime>()
    every { calculator.findNextMonthDayDateTime(capture(event), any(), any(), capture(after)) } returns eventUtc
    val strategy = MonthlyRepeatStrategyV2(manager, calculator)

    strategy.calculateNextOccurrence(reminder("Europe/Berlin", RecurrenceRule.Monthly(dayOfMonth = 6)), eventUtc)

    assertEquals(LocalDateTime.of(2025, 1, 6, 9, 0), event.captured)
    assertEquals(LocalDateTime.of(2025, 1, 6, 11, 0), after.captured)
  }

  @Test
  fun `yearly strategy evaluates a fixed reminder in its own zone`() {
    val event = slot<LocalDateTime>()
    val after = slot<LocalDateTime>()
    every {
      calculator.findNextYearDayDateTime(capture(event), any(), any(), any(), capture(after))
    } returns eventUtc
    val strategy = YearlyRepeatStrategyV2(manager, calculator)

    val rule = RecurrenceRule.Yearly(dayOfMonth = 6, monthOfYear = 0)
    strategy.calculateNextOccurrence(reminder("Europe/Berlin", rule), eventUtc)

    assertEquals(LocalDateTime.of(2025, 1, 6, 9, 0), event.captured)
    assertEquals(LocalDateTime.of(2025, 1, 6, 11, 0), after.captured)
  }

  @Test
  fun `interval strategy adds elapsed time on the utc timeline for a fixed reminder`() {
    val event = slot<LocalDateTime>()
    val after = slot<LocalDateTime>()
    every { calculator.findNextIntervalDateTime(capture(event), any(), capture(after)) } returns
      LocalDateTime.of(2025, 1, 7, 8, 0)
    val strategy = IntervalRepeatStrategyV2(manager, calculator)
    val rule = RecurrenceRule.Daily(repeatInterval = DateTimeManager.DAY)

    val result = strategy.calculateNextOccurrence(reminder("Europe/Berlin", rule), eventUtc)

    assertEquals(eventUtc, event.captured)
    assertEquals(LocalDateTime.of(2025, 1, 6, 10, 0), after.captured)
    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result)
  }

  @Test
  fun `interval strategy uses the real utc now inside the fall back overlap`() {
    every { nowProvider.nowInstant() } returns Instant.parse("2025-10-26T01:30:00Z")
    val after = slot<LocalDateTime>()
    every { calculator.findNextIntervalDateTime(any(), any(), capture(after)) } returns
      LocalDateTime.of(2025, 10, 26, 2, 0)
    val strategy = IntervalRepeatStrategyV2(manager, calculator)
    val rule = RecurrenceRule.Daily(repeatInterval = DateTimeManager.HOUR)
    val event = LocalDateTime.of(2025, 10, 26, 0, 0)

    val result = strategy.calculateNextOccurrence(reminder("Europe/Berlin", rule, event), event)

    assertEquals(LocalDateTime.of(2025, 10, 26, 1, 30), after.captured)
    assertEquals(LocalDateTime.of(2025, 10, 26, 3, 0), result)
  }

  @Test
  fun `weekly strategy sees the gap shifted time not the intended wall clock after a spring gap`() {
    // Known limitation: 02:30 Berlin was stored as 03:30 CEST (01:30Z), so the intended 02:30 is lost.
    val event = slot<LocalDateTime>()
    every { calculator.findNextDayOfWeekDateTime(capture(event), any(), any()) } returns eventUtc
    val strategy = WeekdayRepeatStrategyV2(manager, calculator)
    val stored = LocalDateTime.of(2025, 3, 30, 1, 30)

    strategy.calculateNextOccurrence(
      reminder("Europe/Berlin", RecurrenceRule.Weekly(listOf(1)), stored),
      stored,
    )

    assertEquals(LocalDateTime.of(2025, 3, 30, 3, 30), event.captured)
  }

  @Test
  fun `interval strategy keeps device zone wall clock maths for a floating reminder`() {
    val event = slot<LocalDateTime>()
    val after = slot<LocalDateTime>()
    every { calculator.findNextIntervalDateTime(capture(event), any(), capture(after)) } returns eventUtc
    val strategy = IntervalRepeatStrategyV2(manager, calculator)
    val rule = RecurrenceRule.Daily(repeatInterval = DateTimeManager.DAY)

    strategy.calculateNextOccurrence(reminder(null, rule), eventUtc)

    assertEquals(LocalDateTime.of(2025, 1, 6, 3, 0), event.captured)
    assertEquals(LocalDateTime.of(2025, 1, 6, 5, 0), after.captured)
  }

  @Test
  fun `can start immediately compares a fixed reminder against now in its zone`() {
    val strategy = SimpleDateStrategyV2(manager)
    val future = reminder("Europe/Berlin", eventDateTime = LocalDateTime.of(2025, 1, 6, 10, 30))
    val past = reminder("Europe/Berlin", eventDateTime = LocalDateTime.of(2025, 1, 6, 9, 30))

    assertTrue(strategy.canStartImmediately(future))
    assertFalse(strategy.canStartImmediately(past))
  }

  @Test
  fun `event time of fixed and floating reminders is the stored instant`() {
    val event = LocalDateTime.of(2025, 1, 6, 11, 0)

    val fixed = eventCalculator().calculateEventDateTime(reminder("Europe/Berlin", eventDateTime = event))
    val floating = eventCalculator().calculateEventDateTime(reminder(null, eventDateTime = event))

    assertEquals(millisOf(event), fixed)
    assertEquals(millisOf(event), floating)
  }

  @Test
  fun `event time already in the past is rejected for a fixed reminder`() {
    val event = LocalDateTime.of(2025, 1, 6, 9, 0)

    val result = eventCalculator().calculateEventDateTime(reminder("Europe/Berlin", eventDateTime = event))

    assertNull(result)
  }

  @Test
  fun `complete stores the fixed zone gap occurrence after the gap as utc`() =
    runTest {
      val saved = completeWithNext("Europe/Berlin", LocalDateTime.of(2025, 3, 30, 2, 30))

      assertEquals(LocalDateTime.of(2025, 3, 30, 1, 30), saved.schedule.eventDateTime)
    }

  @Test
  fun `complete stores the fixed zone overlap occurrence with the earlier offset`() =
    runTest {
      val saved = completeWithNext("Europe/Berlin", LocalDateTime.of(2025, 10, 26, 2, 30))

      assertEquals(LocalDateTime.of(2025, 10, 26, 0, 30), saved.schedule.eventDateTime)
    }

  @Test
  fun `complete stores the floating gap occurrence in the device zone`() =
    runTest {
      val saved = completeWithNext(null, LocalDateTime.of(2025, 3, 9, 2, 30))

      assertEquals(LocalDateTime.of(2025, 3, 9, 7, 30), saved.schedule.eventDateTime)
    }

  @Test
  fun `complete stores the floating overlap occurrence with the earlier offset`() =
    runTest {
      val saved = completeWithNext(null, LocalDateTime.of(2025, 11, 2, 1, 30))

      assertEquals(LocalDateTime.of(2025, 11, 2, 5, 30), saved.schedule.eventDateTime)
    }

  @Test
  fun `skip stores the next fixed zone occurrence as utc`() =
    runTest {
      val strategy = mockk<ReminderBehaviorStrategyV2>()
      every { strategy.canSkip(any()) } returns true
      every { strategy.calculateNextOccurrence(any(), any()) } returns LocalDateTime.of(2025, 7, 7, 9, 0)
      val resolver = mockk<BehaviorStrategyResolverV2> { every { resolve(any()) } returns strategy }
      val activate = mockk<ActivateReminderUseCase>()
      val savedSlot = slot<ReminderV2>()
      coEvery { activate(capture(savedSlot), any(), any()) } answers { firstArg() }

      SkipReminderUseCase(resolver, activate, manager)(reminder("Europe/Berlin"))

      assertEquals(LocalDateTime.of(2025, 7, 7, 7, 0), savedSlot.captured.schedule.eventDateTime)
    }

  private suspend fun completeWithNext(
    zoneId: String?,
    next: LocalDateTime,
  ): ReminderV2 {
    val strategy = mockk<ReminderBehaviorStrategyV2>()
    every { strategy.canSkip(any()) } returns true
    every { strategy.calculateNextOccurrence(any(), any()) } returns next
    val resolver = mockk<BehaviorStrategyResolverV2> { every { resolve(any()) } returns strategy }
    val activate = mockk<ActivateReminderUseCase>()
    val savedSlot = slot<ReminderV2>()
    coEvery { activate(capture(savedSlot), any(), any()) } answers { firstArg() }

    CompleteReminderUseCase(
      strategyResolver = resolver,
      deactivateReminderUseCase = mockk(relaxed = true),
      dateTimeManager = manager,
      activateReminderUseCase = activate,
      addReminderToHistoryUseCase = mockk(relaxed = true),
      jobScheduler = mockk(relaxed = true),
      activateChainedRemindersUseCase = mockk(relaxed = true),
    )(reminder(zoneId, RecurrenceRule.Weekly(listOf(1))))

    return savedSlot.captured
  }
}
