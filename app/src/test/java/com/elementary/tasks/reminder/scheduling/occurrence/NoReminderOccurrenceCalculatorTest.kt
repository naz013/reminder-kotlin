package com.elementary.tasks.reminder.scheduling.occurrence

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for NoReminderOccurrenceCalculator.
 *
 * Tests the calculator for reminders with no time-based scheduling.
 * These reminders should always return an empty list of occurrences.
 */
class NoReminderOccurrenceCalculatorTest {

  private lateinit var calculator: NoReminderOccurrenceCalculator

  @Before
  fun setup() {
    calculator = NoReminderOccurrenceCalculator()
  }

  @Test
  fun `calculateOccurrences should return empty list`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Test reminder",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
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
  fun `calculateOccurrences should return empty list with zero occurrences requested`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Test reminder",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
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
  fun `calculateOccurrences should return empty list with negative occurrences requested`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Test reminder",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
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
  fun `calculateOccurrences should return empty list with large number of occurrences`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Test reminder",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 1000

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertTrue(result.isEmpty())
  }
}

