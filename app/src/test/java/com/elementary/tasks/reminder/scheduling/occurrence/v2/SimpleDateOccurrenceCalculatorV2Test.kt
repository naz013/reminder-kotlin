package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for [SimpleDateOccurrenceCalculatorV2], mirroring
 * `SimpleDateOccurrenceCalculatorTest` (the V1 equivalent).
 */
class SimpleDateOccurrenceCalculatorV2Test {
  private lateinit var calculator: SimpleDateOccurrenceCalculatorV2

  @Before
  fun setup() {
    calculator = SimpleDateOccurrenceCalculatorV2()
  }

  private fun reminder(eventDateTime: LocalDateTime? = null) =
    ReminderV2(
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now(), eventDateTime = eventDateTime),
    )

  @Test
  fun `calculateOccurrences should return empty list for simple reminder`() =
    runTest {
      val reminder = reminder(LocalDateTime.of(2025, 1, 15, 10, 0, 0))
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list with zero occurrences requested`() =
    runTest {
      val reminder = reminder()
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list with negative occurrences`() =
    runTest {
      val reminder = reminder()
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, -1)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list regardless of reminder date`() =
    runTest {
      val reminder = reminder(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 10)

      assertTrue(result.isEmpty())
    }
}
