package com.elementary.tasks.reminder.scheduling.occurrence

import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for YearlyRepeatOccurrenceCalculator.
 *
 * Tests the calculator for yearly repeating reminders.
 * Validates occurrence generation for various month and day combinations.
 */
class YearlyRepeatOccurrenceCalculatorTest {
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: YearlyRepeatOccurrenceCalculator

  @Before
  fun setup() {
    recurrenceCalculator = mockk()
    calculator = YearlyRepeatOccurrenceCalculator(recurrenceCalculator)
  }

  @Test
  fun `calculateOccurrences should generate yearly occurrences`() =
    runTest {
      // Arrange - March 15th every year
      val reminder =
        Reminder(
          summary = "Birthday reminder",
          monthOfYear = 2, // March (0-indexed)
          dayOfMonth = 15,
          repeatInterval = 1L,
          syncState = SyncState.Synced,
        )
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
            monthOfYear = reminder.monthOfYear,
            dayOfMonth = reminder.dayOfMonth,
            interval = reminder.repeatInterval,
          )
        } returns occurrence
      }

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertEquals(5, result.size)
      assertEquals(expectedOccurrences, result)
    }

  @Test
  fun `calculateOccurrences should handle leap year date`() =
    runTest {
      // Arrange - February 29th (leap year)
      val reminder =
        Reminder(
          summary = "Leap year birthday",
          monthOfYear = 1, // February (0-indexed)
          dayOfMonth = 29,
          repeatInterval = 1L,
          syncState = SyncState.Synced,
        )
      val fromDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
      val numberOfOccurrences = 3

      val expectedOccurrences =
        listOf(
          LocalDateTime.of(2025, 2, 28, 10, 0, 0), // 2025 is not a leap year, so Feb 28
          LocalDateTime.of(2026, 2, 28, 10, 0, 0), // 2026 is not a leap year
          LocalDateTime.of(2027, 2, 28, 10, 0, 0), // 2027 is not a leap year
        )

      expectedOccurrences.forEachIndexed { index, occurrence ->
        every {
          recurrenceCalculator.getNextYearDayDateTime(
            eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
            monthOfYear = reminder.monthOfYear,
            dayOfMonth = reminder.dayOfMonth,
            interval = reminder.repeatInterval,
          )
        } returns occurrence
      }

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertEquals(3, result.size)
    }

  @Test
  fun `calculateOccurrences should handle multi-year interval`() =
    runTest {
      // Arrange - Every 2 years
      val reminder =
        Reminder(
          summary = "Biennial reminder",
          monthOfYear = 0, // January (0-indexed)
          dayOfMonth = 1,
          repeatInterval = 2L,
          syncState = SyncState.Synced,
        )
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
            monthOfYear = reminder.monthOfYear,
            dayOfMonth = reminder.dayOfMonth,
            interval = reminder.repeatInterval,
          )
        } returns occurrence
      }

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertEquals(4, result.size)
    }

  @Test
  fun `calculateOccurrences should respect repeat limit`() =
    runTest {
      // Arrange
      val reminder =
        Reminder(
          summary = "Limited yearly reminder",
          monthOfYear = 11, // December (0-indexed)
          dayOfMonth = 25,
          repeatInterval = 1L,
          repeatLimit = 15,
          eventCount = 13L,
          syncState = SyncState.Synced,
        )
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
            monthOfYear = reminder.monthOfYear,
            dayOfMonth = reminder.dayOfMonth,
            interval = reminder.repeatInterval,
          )
        } returns occurrence
      }

      // Act - Should only generate 2 occurrences (15 limit - 13 already occurred)
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertEquals(2, result.size)
    }

  @Test
  fun `calculateOccurrences should return empty list when limit exceeded`() =
    runTest {
      // Arrange
      val reminder =
        Reminder(
          summary = "Exhausted yearly reminder",
          monthOfYear = 4,
          dayOfMonth = 6,
          repeatInterval = 1L,
          repeatLimit = 20,
          eventCount = 20L,
          syncState = SyncState.Synced,
        )
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = 5

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for zero occurrences requested`() =
    runTest {
      // Arrange
      val reminder =
        Reminder(
          summary = "Yearly reminder",
          monthOfYear = 0,
          dayOfMonth = 1,
          repeatInterval = 1L,
          syncState = SyncState.Synced,
        )
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = 0

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative occurrences requested`() =
    runTest {
      // Arrange
      val reminder =
        Reminder(
          summary = "Yearly reminder",
          monthOfYear = 0,
          dayOfMonth = 1,
          repeatInterval = 1L,
          syncState = SyncState.Synced,
        )
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = -5

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative dayOfMonth`() =
    runTest {
      // Arrange
      val reminder =
        Reminder(
          summary = "Invalid yearly reminder",
          monthOfYear = 0,
          dayOfMonth = -1,
          repeatInterval = 1L,
          syncState = SyncState.Synced,
        )
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = 5

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list for negative monthOfYear`() =
    runTest {
      // Arrange
      val reminder =
        Reminder(
          summary = "Invalid yearly reminder",
          monthOfYear = -1,
          dayOfMonth = 1,
          repeatInterval = 1L,
          syncState = SyncState.Synced,
        )
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
      val numberOfOccurrences = 5

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() =
    runTest {
      // Arrange
      val reminder =
        Reminder(
          summary = "Unlimited yearly reminder",
          monthOfYear = 0,
          dayOfMonth = 1,
          repeatInterval = 1L,
          repeatLimit = -1,
          eventCount = 10L,
          syncState = SyncState.Synced,
        )
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
            monthOfYear = reminder.monthOfYear,
            dayOfMonth = reminder.dayOfMonth,
            interval = reminder.repeatInterval,
          )
        } returns occurrence
      }

      // Act
      val result =
        calculator.calculateOccurrences(
          reminder,
          fromDateTime,
          numberOfOccurrences,
        )

      // Assert
      assertEquals(5, result.size)
    }
}
