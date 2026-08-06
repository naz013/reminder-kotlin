package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Unit tests for [TimerRepeatOccurrenceCalculatorV2], mirroring
 * `TimerRepeatOccurrenceCalculatorTest` (the V1 equivalent).
 */
class TimerRepeatOccurrenceCalculatorV2Test {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: TimerRepeatOccurrenceCalculatorV2

  @Before
  fun setup() {
    dateTimeManager = mockk()
    recurrenceCalculator = mockk()
    calculator = TimerRepeatOccurrenceCalculatorV2(dateTimeManager, recurrenceCalculator)
  }

  private fun reminder(
    repeatInterval: Long,
    from: String,
    to: String,
    hours: List<Int> = emptyList(),
    repeatLimit: Int = -1,
    eventCount: Long = 0L,
  ) = ReminderV2(
    recurrence = RecurrenceRule.Countdown(after = 1000L, repeatInterval = repeatInterval, repeatLimit = repeatLimit),
    notification = NotificationSettingsOverride(quietHoursFrom = from, quietHoursTo = to, activeHours = hours),
    eventCount = eventCount,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
  )

  @Test
  fun `calculateOccurrences should generate timer occurrences`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L, from = "09:00", to = "17:00", hours = listOf(12, 13))
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
      val numberOfOccurrences = 5

      val fromTime = LocalTime.of(9, 0, 0)
      val toTime = LocalTime.of(17, 0, 0)

      every { dateTimeManager.toLocalTime("09:00") } returns fromTime
      every { dateTimeManager.toLocalTime("17:00") } returns toTime

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 6, 10, 0, 0),
          LocalDateTime.of(2025, 1, 6, 11, 0, 0),
          LocalDateTime.of(2025, 1, 6, 14, 0, 0),
          LocalDateTime.of(2025, 1, 6, 15, 0, 0),
          LocalDateTime.of(2025, 1, 6, 16, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextTimerDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            interval = 3600000L,
            excludedHours = listOf(12, 13),
            excludedFromTime = fromTime,
            excludedToTime = toTime,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(5, result.size)
      assertEquals(expectedOccurrences, result)
    }

  @Test
  fun `calculateOccurrences should respect repeat limit`() =
    runTest {
      val reminder = reminder(repeatInterval = 1800000L, from = "08:00", to = "18:00", repeatLimit = 20, eventCount = 17L)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 8, 0, 0)
      val numberOfOccurrences = 10

      val fromTime = LocalTime.of(8, 0, 0)
      val toTime = LocalTime.of(18, 0, 0)

      every { dateTimeManager.toLocalTime("08:00") } returns fromTime
      every { dateTimeManager.toLocalTime("18:00") } returns toTime

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 6, 8, 30, 0),
          LocalDateTime.of(2025, 1, 6, 9, 0, 0),
          LocalDateTime.of(2025, 1, 6, 9, 30, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextTimerDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            interval = 1800000L,
            excludedHours = emptyList(),
            excludedFromTime = fromTime,
            excludedToTime = toTime,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(3, result.size)
    }

  @Test
  fun `calculateOccurrences should return empty list when limit exceeded`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L, from = "09:00", to = "17:00", repeatLimit = 50, eventCount = 50L)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      every { dateTimeManager.toLocalTime("09:00") } returns LocalTime.of(9, 0, 0)
      every { dateTimeManager.toLocalTime("17:00") } returns LocalTime.of(17, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero occurrences requested`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L, from = "09:00", to = "17:00")
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative occurrences requested`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L, from = "09:00", to = "17:00")
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, -5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero repeat interval`() =
    runTest {
      val reminder = reminder(repeatInterval = 0L, from = "09:00", to = "17:00")
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative repeat interval`() =
    runTest {
      val reminder = reminder(repeatInterval = -3600000L, from = "09:00", to = "17:00")
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should handle overnight time window`() =
    runTest {
      val reminder = reminder(repeatInterval = 7200000L, from = "22:00", to = "06:00")
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 22, 0, 0)
      val numberOfOccurrences = 3

      val fromTime = LocalTime.of(22, 0, 0)
      val toTime = LocalTime.of(6, 0, 0)

      every { dateTimeManager.toLocalTime("22:00") } returns fromTime
      every { dateTimeManager.toLocalTime("06:00") } returns toTime

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 7, 0, 0, 0),
          LocalDateTime.of(2025, 1, 7, 2, 0, 0),
          LocalDateTime.of(2025, 1, 7, 4, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextTimerDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            interval = 7200000L,
            excludedHours = emptyList(),
            excludedFromTime = fromTime,
            excludedToTime = toTime,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(3, result.size)
    }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L, from = "09:00", to = "17:00", repeatLimit = -1, eventCount = 100L)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
      val numberOfOccurrences = 5

      val fromTime = LocalTime.of(9, 0, 0)
      val toTime = LocalTime.of(17, 0, 0)

      every { dateTimeManager.toLocalTime("09:00") } returns fromTime
      every { dateTimeManager.toLocalTime("17:00") } returns toTime

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 6, 10, 0, 0),
          LocalDateTime.of(2025, 1, 6, 11, 0, 0),
          LocalDateTime.of(2025, 1, 6, 12, 0, 0),
          LocalDateTime.of(2025, 1, 6, 13, 0, 0),
          LocalDateTime.of(2025, 1, 6, 14, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextTimerDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            interval = 3600000L,
            excludedHours = emptyList(),
            excludedFromTime = fromTime,
            excludedToTime = toTime,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(5, result.size)
    }
}
