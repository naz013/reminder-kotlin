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
 * Unit tests for [IntervalRepeatOccurrenceCalculatorV2], mirroring
 * `IntervalRepeatOccurrenceCalculatorTest` (the V1 equivalent).
 */
class IntervalRepeatOccurrenceCalculatorV2Test {
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: IntervalRepeatOccurrenceCalculatorV2

  @Before
  fun setup() {
    recurrenceCalculator = mockk()
    calculator = IntervalRepeatOccurrenceCalculatorV2(recurrenceCalculator)
  }

  private fun reminder(
    repeatInterval: Long,
    repeatLimit: Int = -1,
    eventCount: Long = 0L,
  ) = ReminderV2(
    recurrence = RecurrenceRule.Daily(repeatInterval = repeatInterval, repeatLimit = repeatLimit),
    eventCount = eventCount,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
  )

  @Test
  fun `calculateOccurrences should generate correct number of occurrences`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 5

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 1, 11, 0, 0),
          LocalDateTime.of(2025, 1, 1, 12, 0, 0),
          LocalDateTime.of(2025, 1, 1, 13, 0, 0),
          LocalDateTime.of(2025, 1, 1, 14, 0, 0),
          LocalDateTime.of(2025, 1, 1, 15, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextIntervalDateTime(
            if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            3600000L,
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
      val reminder = reminder(repeatInterval = 3600000L, repeatLimit = 10, eventCount = 7L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 10

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 1, 11, 0, 0),
          LocalDateTime.of(2025, 1, 1, 12, 0, 0),
          LocalDateTime.of(2025, 1, 1, 13, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextIntervalDateTime(
            if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            3600000L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(3, result.size)
    }

  @Test
  fun `calculateOccurrences should return empty list when limit exceeded`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L, repeatLimit = 10, eventCount = 10L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list when limit exceeded beyond`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L, repeatLimit = 10, eventCount = 15L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero occurrences requested`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative occurrences requested`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, -5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero repeat interval`() =
    runTest {
      val reminder = reminder(repeatInterval = 0L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative repeat interval`() =
    runTest {
      val reminder = reminder(repeatInterval = -3600000L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should handle large intervals`() =
    runTest {
      val reminder = reminder(repeatInterval = 86400000L * 30)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 3

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 31, 10, 0, 0),
          LocalDateTime.of(2025, 3, 2, 10, 0, 0),
          LocalDateTime.of(2025, 4, 1, 10, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextIntervalDateTime(
            if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            86400000L * 30,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(3, result.size)
      assertEquals(expectedOccurrences, result)
    }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() =
    runTest {
      val reminder = reminder(repeatInterval = 3600000L, repeatLimit = -1, eventCount = 100L)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 5

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 1, 11, 0, 0),
          LocalDateTime.of(2025, 1, 1, 12, 0, 0),
          LocalDateTime.of(2025, 1, 1, 13, 0, 0),
          LocalDateTime.of(2025, 1, 1, 14, 0, 0),
          LocalDateTime.of(2025, 1, 1, 15, 0, 0),
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextIntervalDateTime(
            if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            3600000L,
          )
        } returns occurrence
      }

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(5, result.size)
    }
}
