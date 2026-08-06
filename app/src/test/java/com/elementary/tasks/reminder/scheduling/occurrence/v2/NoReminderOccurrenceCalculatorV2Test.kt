package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for [NoReminderOccurrenceCalculatorV2], mirroring
 * `NoReminderOccurrenceCalculatorTest` (the V1 equivalent).
 */
class NoReminderOccurrenceCalculatorV2Test {
  private lateinit var calculator: NoReminderOccurrenceCalculatorV2

  @Before
  fun setup() {
    calculator = NoReminderOccurrenceCalculatorV2()
  }

  private fun reminder() = ReminderV2(schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  @Test
  fun `calculateOccurrences should return empty list`() =
    runTest {
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder(), fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list with zero occurrences requested`() =
    runTest {
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder(), fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list with negative occurrences requested`() =
    runTest {
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder(), fromDateTime, -5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list with large number of occurrences`() =
    runTest {
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder(), fromDateTime, 1000)

      assertTrue(result.isEmpty())
    }
}
