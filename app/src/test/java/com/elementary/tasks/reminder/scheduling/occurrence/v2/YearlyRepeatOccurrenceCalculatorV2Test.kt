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
 * Unit tests for [YearlyRepeatOccurrenceCalculatorV2], mirroring
 * `YearlyRepeatOccurrenceCalculatorTest` (the V1 equivalent).
 */
class YearlyRepeatOccurrenceCalculatorV2Test {
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: YearlyRepeatOccurrenceCalculatorV2

  @Before
  fun setup() {
    recurrenceCalculator = mockk()
    calculator = YearlyRepeatOccurrenceCalculatorV2(recurrenceCalculator)
  }

  private fun reminder(
    monthOfYear: Int,
    dayOfMonth: Int,
    repeatInterval: Long = 1L,
    repeatLimit: Int = -1,
    eventCount: Long = 0L,
  ) = ReminderV2(
    recurrence =
      RecurrenceRule.Yearly(
        dayOfMonth = dayOfMonth,
        monthOfYear = monthOfYear,
        repeatInterval = repeatInterval,
        repeatLimit = repeatLimit,
      ),
    eventCount = eventCount,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
  )

  @Test
  fun `calculateOccurrences should generate yearly occurrences`() =
    runTest {
      val reminder = reminder(monthOfYear = 2, dayOfMonth = 15)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 5

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2026, 3, 15, 10, 0, 0),
          LocalDateTime.of(2027, 3, 15, 10, 0, 0),
          LocalDateTime.of(2028, 3, 15, 10, 0, 0),
          LocalDateTime.of(2029, 3, 15, 10, 0, 0),
          LocalDateTime.of(2030, 3, 15, 10, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextYearDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            monthOfYear = 2,
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
  fun `calculateOccurrences should handle leap year date`() =
    runTest {
      val reminder = reminder(monthOfYear = 1, dayOfMonth = 29)
      val fromDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 3

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 2, 28, 10, 0, 0),
          LocalDateTime.of(2026, 2, 28, 10, 0, 0),
          LocalDateTime.of(2027, 2, 28, 10, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextYearDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            monthOfYear = 1,
            dayOfMonth = 29,
            interval = 1L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(3, result.size)
    }

  @Test
  fun `calculateOccurrences should handle multi-year interval`() =
    runTest {
      val reminder = reminder(monthOfYear = 0, dayOfMonth = 1, repeatInterval = 2L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = 4

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2027, 1, 1, 0, 0, 0),
          LocalDateTime.of(2029, 1, 1, 0, 0, 0),
          LocalDateTime.of(2031, 1, 1, 0, 0, 0),
          LocalDateTime.of(2033, 1, 1, 0, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextYearDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            monthOfYear = 0,
            dayOfMonth = 1,
            interval = 2L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(4, result.size)
    }

  @Test
  fun `calculateOccurrences should respect repeat limit`() =
    runTest {
      val reminder = reminder(monthOfYear = 11, dayOfMonth = 25, repeatLimit = 15, eventCount = 13L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = 10

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2026, 12, 25, 0, 0, 0),
          LocalDateTime.of(2027, 12, 25, 0, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextYearDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            monthOfYear = 11,
            dayOfMonth = 25,
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
      val reminder = reminder(monthOfYear = 4, dayOfMonth = 6, repeatLimit = 20, eventCount = 20L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero occurrences requested`() =
    runTest {
      val reminder = reminder(monthOfYear = 0, dayOfMonth = 1)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative occurrences requested`() =
    runTest {
      val reminder = reminder(monthOfYear = 0, dayOfMonth = 1)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, -5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative dayOfMonth`() =
    runTest {
      val reminder = reminder(monthOfYear = 0, dayOfMonth = -1)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative monthOfYear`() =
    runTest {
      val reminder = reminder(monthOfYear = -1, dayOfMonth = 1)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() =
    runTest {
      val reminder = reminder(monthOfYear = 0, dayOfMonth = 1, repeatLimit = -1, eventCount = 10L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = 5

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2026, 1, 1, 0, 0, 0),
          LocalDateTime.of(2027, 1, 1, 0, 0, 0),
          LocalDateTime.of(2028, 1, 1, 0, 0, 0),
          LocalDateTime.of(2029, 1, 1, 0, 0, 0),
          LocalDateTime.of(2030, 1, 1, 0, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextYearDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            monthOfYear = 0,
            dayOfMonth = 1,
            interval = 1L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(5, result.size)
    }
}
