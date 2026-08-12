package com.github.naz013.logic.reminder.occurrence

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
 * Unit tests for [MonthlyRepeatOccurrenceCalculatorV2], mirroring
 * `MonthlyRepeatOccurrenceCalculatorTest` (the V1 equivalent).
 */
class MonthlyRepeatOccurrenceCalculatorV2Test {
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: MonthlyRepeatOccurrenceCalculatorV2

  @Before
  fun setup() {
    recurrenceCalculator = mockk()
    calculator = MonthlyRepeatOccurrenceCalculatorV2(recurrenceCalculator)
  }

  private fun reminder(
    dayOfMonth: Int,
    repeatInterval: Long = 1L,
    repeatLimit: Int = -1,
    eventCount: Long = 0L,
  ) = ReminderV2(
    recurrence = RecurrenceRule.Monthly(dayOfMonth = dayOfMonth, repeatInterval = repeatInterval, repeatLimit = repeatLimit),
    eventCount = eventCount,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
  )

  @Test
  fun `calculateOccurrences should generate monthly occurrences`() =
    runTest {
      val reminder = reminder(dayOfMonth = 15)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 5

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 2, 15, 10, 0, 0),
          LocalDateTime.of(2025, 3, 15, 10, 0, 0),
          LocalDateTime.of(2025, 4, 15, 10, 0, 0),
          LocalDateTime.of(2025, 5, 15, 10, 0, 0),
          LocalDateTime.of(2025, 6, 15, 10, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextMonthDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            dayOfMonth = 15,
            interval = 1L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(5, result.size)
      assertEquals(expectedOccurrences, result)
    }

  @Test
  fun `calculateOccurrences should handle last day of month`() =
    runTest {
      val reminder = reminder(dayOfMonth = 0)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 3

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 2, 28, 10, 0, 0),
          LocalDateTime.of(2025, 3, 31, 10, 0, 0),
          LocalDateTime.of(2025, 4, 30, 10, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextMonthDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            dayOfMonth = 0,
            interval = 1L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(3, result.size)
    }

  @Test
  fun `calculateOccurrences should handle multi-month interval`() =
    runTest {
      val reminder = reminder(dayOfMonth = 10, repeatInterval = 3L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 14, 0, 0)
      val numberOfOccurrences = 4

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 4, 10, 14, 0, 0),
          LocalDateTime.of(2025, 7, 10, 14, 0, 0),
          LocalDateTime.of(2025, 10, 10, 14, 0, 0),
          LocalDateTime.of(2026, 1, 10, 14, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextMonthDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            dayOfMonth = 10,
            interval = 3L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(4, result.size)
    }

  @Test
  fun `calculateOccurrences should respect repeat limit`() =
    runTest {
      val reminder = reminder(dayOfMonth = 20, repeatLimit = 10, eventCount = 8L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
      val numberOfOccurrences = 10

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 2, 20, 9, 0, 0),
          LocalDateTime.of(2025, 3, 20, 9, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextMonthDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            dayOfMonth = 20,
            interval = 1L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(2, result.size)
    }

  @Test
  fun `calculateOccurrences should return empty list when limit exceeded`() =
    runTest {
      val reminder = reminder(dayOfMonth = 5, repeatLimit = 12, eventCount = 12L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero occurrences requested`() =
    runTest {
      val reminder = reminder(dayOfMonth = 15)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative occurrences requested`() =
    runTest {
      val reminder = reminder(dayOfMonth = 15)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, -5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative dayOfMonth`() =
    runTest {
      val reminder = reminder(dayOfMonth = -5)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() =
    runTest {
      val reminder = reminder(dayOfMonth = 1, repeatLimit = -1, eventCount = 50L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = 5

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 2, 1, 0, 0, 0),
          LocalDateTime.of(2025, 3, 1, 0, 0, 0),
          LocalDateTime.of(2025, 4, 1, 0, 0, 0),
          LocalDateTime.of(2025, 5, 1, 0, 0, 0),
          LocalDateTime.of(2025, 6, 1, 0, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextMonthDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            dayOfMonth = 1,
            interval = 1L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(5, result.size)
    }
}
