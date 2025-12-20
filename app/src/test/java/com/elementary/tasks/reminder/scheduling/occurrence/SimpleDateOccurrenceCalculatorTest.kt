package com.elementary.tasks.reminder.scheduling.occurrence

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for SimpleDateOccurrenceCalculator.
 *
 * Tests the calculator for simple one-time date/time reminders.
 * Since these reminders have no repeat pattern, they should return an empty list.
 */
class SimpleDateOccurrenceCalculatorTest {

  private lateinit var calculator: SimpleDateOccurrenceCalculator

  @Before
  fun setup() {
    calculator = SimpleDateOccurrenceCalculator()
  }

  @Test
  fun `calculateOccurrences should return empty list for simple reminder`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "One-time reminder",
      eventTime = "2025-01-15 10:00:00",
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
      summary = "One-time reminder",
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
  fun `calculateOccurrences should return empty list with negative occurrences`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "One-time reminder",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = -1

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
  fun `calculateOccurrences should return empty list regardless of reminder date`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "One-time reminder",
      eventTime = "2025-12-31 23:59:59",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    val numberOfOccurrences = 10

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

