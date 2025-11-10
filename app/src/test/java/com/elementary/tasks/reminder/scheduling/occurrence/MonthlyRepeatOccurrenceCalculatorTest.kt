package com.elementary.tasks.reminder.scheduling.occurrence

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
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
 * Unit tests for MonthlyRepeatOccurrenceCalculator.
 *
 * Tests the calculator for monthly repeating reminders.
 * Validates occurrence generation for various day-of-month scenarios.
 */
class MonthlyRepeatOccurrenceCalculatorTest {

  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: MonthlyRepeatOccurrenceCalculator

  @Before
  fun setup() {
    recurrenceCalculator = mockk()
    calculator = MonthlyRepeatOccurrenceCalculator(recurrenceCalculator)
  }

  @Test
  fun `calculateOccurrences should generate monthly occurrences`() = runTest {
    // Arrange - 15th of every month
    val reminder = Reminder(
      summary = "Monthly reminder",
      dayOfMonth = 15,
      repeatInterval = 1L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 5

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 2, 15, 10, 0, 0),
      LocalDateTime.of(2025, 3, 15, 10, 0, 0),
      LocalDateTime.of(2025, 4, 15, 10, 0, 0),
      LocalDateTime.of(2025, 5, 15, 10, 0, 0),
      LocalDateTime.of(2025, 6, 15, 10, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextMonthDayDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          dayOfMonth = reminder.dayOfMonth,
          interval = reminder.repeatInterval
        )
      } returns occurrence
    }

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertEquals(5, result.size)
    assertEquals(expectedOccurrences, result)
  }

  @Test
  fun `calculateOccurrences should handle last day of month`() = runTest {
    // Arrange - Last day of every month (dayOfMonth = 0)
    val reminder = Reminder(
      summary = "End of month reminder",
      dayOfMonth = 0, // Last day of month
      repeatInterval = 1L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 3

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 2, 28, 10, 0, 0), // Feb has 28 days in 2025
      LocalDateTime.of(2025, 3, 31, 10, 0, 0), // March has 31 days
      LocalDateTime.of(2025, 4, 30, 10, 0, 0)  // April has 30 days
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextMonthDayDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          dayOfMonth = reminder.dayOfMonth,
          interval = reminder.repeatInterval
        )
      } returns occurrence
    }

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertEquals(3, result.size)
  }

  @Test
  fun `calculateOccurrences should handle multi-month interval`() = runTest {
    // Arrange - Every 3 months
    val reminder = Reminder(
      summary = "Quarterly reminder",
      dayOfMonth = 10,
      repeatInterval = 3L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 14, 0, 0)
    val numberOfOccurrences = 4

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 4, 10, 14, 0, 0),
      LocalDateTime.of(2025, 7, 10, 14, 0, 0),
      LocalDateTime.of(2025, 10, 10, 14, 0, 0),
      LocalDateTime.of(2026, 1, 10, 14, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextMonthDayDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          dayOfMonth = reminder.dayOfMonth,
          interval = reminder.repeatInterval
        )
      } returns occurrence
    }

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertEquals(4, result.size)
  }

  @Test
  fun `calculateOccurrences should respect repeat limit`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Limited monthly reminder",
      dayOfMonth = 20,
      repeatInterval = 1L,
      repeatLimit = 10,
      eventCount = 8L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
    val numberOfOccurrences = 10

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 2, 20, 9, 0, 0),
      LocalDateTime.of(2025, 3, 20, 9, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextMonthDayDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          dayOfMonth = reminder.dayOfMonth,
          interval = reminder.repeatInterval
        )
      } returns occurrence
    }

    // Act - Should only generate 2 occurrences (10 limit - 8 already occurred)
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertEquals(2, result.size)
  }

  @Test
  fun `calculateOccurrences should return empty list when limit exceeded`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Exhausted monthly reminder",
      dayOfMonth = 5,
      repeatInterval = 1L,
      repeatLimit = 12,
      eventCount = 12L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
    val numberOfOccurrences = 5

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun `calculateOccurrences should return empty list for zero occurrences requested`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Monthly reminder",
      dayOfMonth = 15,
      repeatInterval = 1L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
    val numberOfOccurrences = 0

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun `calculateOccurrences should return empty list for negative occurrences requested`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Monthly reminder",
      dayOfMonth = 15,
      repeatInterval = 1L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
    val numberOfOccurrences = -5

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun `calculateOccurrences should return empty list for negative dayOfMonth`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Invalid monthly reminder",
      dayOfMonth = -5,
      repeatInterval = 1L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
    val numberOfOccurrences = 5

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Unlimited monthly reminder",
      dayOfMonth = 1,
      repeatInterval = 1L,
      repeatLimit = -1,
      eventCount = 50L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    val numberOfOccurrences = 5

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 2, 1, 0, 0, 0),
      LocalDateTime.of(2025, 3, 1, 0, 0, 0),
      LocalDateTime.of(2025, 4, 1, 0, 0, 0),
      LocalDateTime.of(2025, 5, 1, 0, 0, 0),
      LocalDateTime.of(2025, 6, 1, 0, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextMonthDayDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          dayOfMonth = reminder.dayOfMonth,
          interval = reminder.repeatInterval
        )
      } returns occurrence
    }

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertEquals(5, result.size)
  }
}

