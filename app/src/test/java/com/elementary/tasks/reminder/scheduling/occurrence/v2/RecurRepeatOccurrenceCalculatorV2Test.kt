package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.elementary.tasks.core.utils.datetime.RecurEventManager
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
 * Unit tests for [RecurRepeatOccurrenceCalculatorV2], mirroring
 * `RecurRepeatOccurrenceCalculatorTest` (the V1 equivalent).
 */
class RecurRepeatOccurrenceCalculatorV2Test {
  private lateinit var recurEventManager: RecurEventManager
  private lateinit var calculator: RecurRepeatOccurrenceCalculatorV2

  @Before
  fun setup() {
    recurEventManager = mockk()
    calculator = RecurRepeatOccurrenceCalculatorV2(recurEventManager)
  }

  private fun reminder(rrule: String?) =
    ReminderV2(
      recurrence = if (rrule != null) RecurrenceRule.ICalendar(rrule) else RecurrenceRule.Once,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )

  @Test
  fun `calculateOccurrences should generate occurrences from RRULE`() =
    runTest {
      val rrule = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
      val reminder = reminder(rrule)
      val fromDateTime = LocalDateTime.of(2025, 1, 6, 10, 0, 0)
      val numberOfOccurrences = 4

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 7, 10, 0, 0),
          LocalDateTime.of(2025, 1, 9, 10, 0, 0),
          LocalDateTime.of(2025, 1, 14, 10, 0, 0),
          LocalDateTime.of(2025, 1, 16, 10, 0, 0),
        )

      every { recurEventManager.getNextAfterDateTime(fromDateTime, rrule) } returns expectedOccurrences[0]
      every { recurEventManager.getNextAfterDateTime(expectedOccurrences[0].plusSeconds(1), rrule) } returns expectedOccurrences[1]
      every { recurEventManager.getNextAfterDateTime(expectedOccurrences[1].plusSeconds(1), rrule) } returns expectedOccurrences[2]
      every { recurEventManager.getNextAfterDateTime(expectedOccurrences[2].plusSeconds(1), rrule) } returns expectedOccurrences[3]

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(4, result.size)
      assertEquals(expectedOccurrences, result)
    }

  @Test
  fun `calculateOccurrences should handle limited RRULE with COUNT`() =
    runTest {
      val rrule = "RRULE:FREQ=DAILY;COUNT=5"
      val reminder = reminder(rrule)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
      val numberOfOccurrences = 10

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 2, 9, 0, 0),
          LocalDateTime.of(2025, 1, 3, 9, 0, 0),
          LocalDateTime.of(2025, 1, 4, 9, 0, 0),
        )

      every { recurEventManager.getNextAfterDateTime(fromDateTime, rrule) } returns expectedOccurrences[0]
      every { recurEventManager.getNextAfterDateTime(expectedOccurrences[0].plusSeconds(1), rrule) } returns expectedOccurrences[1]
      every { recurEventManager.getNextAfterDateTime(expectedOccurrences[1].plusSeconds(1), rrule) } returns expectedOccurrences[2]
      every { recurEventManager.getNextAfterDateTime(expectedOccurrences[2].plusSeconds(1), rrule) } returns null

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(3, result.size)
    }

  @Test
  fun `calculateOccurrences should return empty list for empty rrule`() =
    runTest {
      val reminder = reminder("")
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for non-ICalendar recurrence`() =
    runTest {
      val reminder = reminder(null)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero occurrences requested`() =
    runTest {
      val reminder = reminder("RRULE:FREQ=DAILY")
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative occurrences requested`() =
    runTest {
      val reminder = reminder("RRULE:FREQ=DAILY")
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, -5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should stop early when RRULE ends`() =
    runTest {
      val rrule = "RRULE:FREQ=DAILY;COUNT=2"
      val reminder = reminder(rrule)
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 10

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 1, 2, 10, 0, 0),
          LocalDateTime.of(2025, 1, 3, 10, 0, 0),
        )

      every { recurEventManager.getNextAfterDateTime(fromDateTime, rrule) } returns expectedOccurrences[0]
      every { recurEventManager.getNextAfterDateTime(expectedOccurrences[0].plusSeconds(1), rrule) } returns expectedOccurrences[1]
      every { recurEventManager.getNextAfterDateTime(expectedOccurrences[1].plusSeconds(1), rrule) } returns null

      val result = calculator.calculateOccurrences(reminder, fromDateTime, numberOfOccurrences)

      assertEquals(2, result.size)
    }
}
