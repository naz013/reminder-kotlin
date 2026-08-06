package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.github.naz013.datecalc.RecurrenceCalculator
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

/**
 * Unit tests for [WeekdayRepeatOccurrenceCalculatorV2], mirroring
 * `WeekdayRepeatOccurrenceCalculatorTest` (the V1 equivalent).
 */
class WeekdayRepeatOccurrenceCalculatorV2Test {
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: WeekdayRepeatOccurrenceCalculatorV2

  @Before
  fun setup() {
    recurrenceCalculator = mockk()
    calculator = WeekdayRepeatOccurrenceCalculatorV2(recurrenceCalculator)
  }

  private fun reminder(
    weekdays: List<Int>,
    repeatLimit: Int = -1,
    eventCount: Long = 0L,
  ) = ReminderV2(
    recurrence = RecurrenceRule.Weekly(weekdays = weekdays, repeatLimit = repeatLimit),
    eventCount = eventCount,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
  )

  @Test
  fun `calculateOccurrences should generate occurrences for selected weekdays`() =
    runTest {
      val weekdays = listOf(0, 1, 0, 1, 0, 0, 0)
      val reminder = reminder(weekdays = weekdays)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 10, 0, 0)
      val numberOfOccurrences = 4

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 8, 10, 0, 0),
          LocalDateTime.of(2025, 1, 13, 10, 0, 0),
          LocalDateTime.of(2025, 1, 15, 10, 0, 0),
          LocalDateTime.of(2025, 1, 20, 10, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextDayOfWeekDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            weekdays = weekdays,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(4, result.size)
      assertEquals(expectedOccurrences, result)
    }

  @Test
  fun `calculateOccurrences should respect repeat limit`() =
    runTest {
      val weekdays = listOf(0, 1, 1, 1, 1, 1, 0)
      val reminder = reminder(weekdays = weekdays, repeatLimit = 15, eventCount = 12L)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
      val numberOfOccurrences = 10

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 7, 9, 0, 0),
          LocalDateTime.of(2025, 1, 8, 9, 0, 0),
          LocalDateTime.of(2025, 1, 9, 9, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextDayOfWeekDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            weekdays = weekdays,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(3, result.size)
    }

  @Test
  fun `calculateOccurrences should return empty list when limit exceeded`() =
    runTest {
      val reminder = reminder(weekdays = listOf(0, 1, 1, 1, 1, 1, 0), repeatLimit = 20, eventCount = 20L)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for empty weekdays`() =
    runTest {
      val reminder = reminder(weekdays = emptyList())
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero occurrences requested`() =
    runTest {
      val reminder = reminder(weekdays = listOf(0, 1, 0, 1, 0, 0, 0))
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative occurrences requested`() =
    runTest {
      val reminder = reminder(weekdays = listOf(0, 1, 0, 1, 0, 0, 0))
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, -5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should handle weekend only reminder`() =
    runTest {
      val weekdays = listOf(1, 0, 0, 0, 0, 0, 1)
      val reminder = reminder(weekdays = weekdays)
      val fromDateTime = LocalDateTime.of(2025, 1, 10, 10, 0, 0)
      val numberOfOccurrences = 4

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 11, 10, 0, 0),
          LocalDateTime.of(2025, 1, 12, 10, 0, 0),
          LocalDateTime.of(2025, 1, 18, 10, 0, 0),
          LocalDateTime.of(2025, 1, 19, 10, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextDayOfWeekDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            weekdays = weekdays,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(4, result.size)
    }

  @Test
  fun `calculateOccurrences should handle every day reminder`() =
    runTest {
      val weekdays = listOf(1, 1, 1, 1, 1, 1, 1)
      val reminder = reminder(weekdays = weekdays)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 8, 30, 0)
      val numberOfOccurrences = 7

      val expectedOccurrences = (1..7).map { LocalDateTime.of(2025, 1, 6 + it, 8, 30, 0) }

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextDayOfWeekDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            weekdays = weekdays,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(7, result.size)
    }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() =
    runTest {
      val weekdays = listOf(0, 1, 0, 0, 0, 0, 0)
      val reminder = reminder(weekdays = weekdays, repeatLimit = -1, eventCount = 50L)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
      val numberOfOccurrences = 5

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 13, 9, 0, 0),
          LocalDateTime.of(2025, 1, 20, 9, 0, 0),
          LocalDateTime.of(2025, 1, 27, 9, 0, 0),
          LocalDateTime.of(2025, 2, 3, 9, 0, 0),
          LocalDateTime.of(2025, 2, 10, 9, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextDayOfWeekDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            weekdays = weekdays,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(5, result.size)
    }
}
