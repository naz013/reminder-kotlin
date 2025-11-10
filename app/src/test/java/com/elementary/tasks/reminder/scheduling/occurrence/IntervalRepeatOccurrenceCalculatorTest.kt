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
 * Unit tests for IntervalRepeatOccurrenceCalculator.
 *
 * Tests the calculator for interval-based repeating reminders.
 * Validates occurrence generation with various intervals and limits.
 */
class IntervalRepeatOccurrenceCalculatorTest {

  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: IntervalRepeatOccurrenceCalculator

  @Before
  fun setup() {
    recurrenceCalculator = mockk()
    calculator = IntervalRepeatOccurrenceCalculator(recurrenceCalculator)
  }

  @Test
  fun `calculateOccurrences should generate correct number of occurrences`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Interval reminder",
      repeatInterval = 3600000L, // 1 hour in milliseconds
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 5

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 1, 11, 0, 0),
      LocalDateTime.of(2025, 1, 1, 12, 0, 0),
      LocalDateTime.of(2025, 1, 1, 13, 0, 0),
      LocalDateTime.of(2025, 1, 1, 14, 0, 0),
      LocalDateTime.of(2025, 1, 1, 15, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextIntervalDateTime(
          if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          reminder.repeatInterval
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
  fun `calculateOccurrences should respect repeat limit`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Limited interval reminder",
      repeatInterval = 3600000L, // 1 hour
      repeatLimit = 10,
      eventCount = 7L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 10

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 1, 11, 0, 0),
      LocalDateTime.of(2025, 1, 1, 12, 0, 0),
      LocalDateTime.of(2025, 1, 1, 13, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextIntervalDateTime(
          if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          reminder.repeatInterval
        )
      } returns occurrence
    }

    // Act - Should only generate 3 occurrences (10 limit - 7 already occurred)
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert
    assertEquals(3, result.size)
  }

  @Test
  fun `calculateOccurrences should return empty list when limit exceeded`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Exhausted interval reminder",
      repeatInterval = 3600000L,
      repeatLimit = 10,
      eventCount = 10L, // Already at limit
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
  fun `calculateOccurrences should return empty list when limit exceeded beyond`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Over limit interval reminder",
      repeatInterval = 3600000L,
      repeatLimit = 10,
      eventCount = 15L, // Beyond limit
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
  fun `calculateOccurrences should return empty list for zero occurrences requested`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Interval reminder",
      repeatInterval = 3600000L,
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
  fun `calculateOccurrences should return empty list for negative occurrences requested`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Interval reminder",
      repeatInterval = 3600000L,
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
  fun `calculateOccurrences should return empty list for zero repeat interval`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Invalid interval reminder",
      repeatInterval = 0L,
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
  fun `calculateOccurrences should return empty list for negative repeat interval`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Invalid interval reminder",
      repeatInterval = -3600000L,
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
  fun `calculateOccurrences should handle large intervals`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Large interval reminder",
      repeatInterval = 86400000L * 30, // 30 days in milliseconds
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 3

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 31, 10, 0, 0),
      LocalDateTime.of(2025, 3, 2, 10, 0, 0),
      LocalDateTime.of(2025, 4, 1, 10, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextIntervalDateTime(
          if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          reminder.repeatInterval
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
    assertEquals(expectedOccurrences, result)
  }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Unlimited interval reminder",
      repeatInterval = 3600000L,
      repeatLimit = -1, // Unlimited
      eventCount = 100L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 5

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 1, 11, 0, 0),
      LocalDateTime.of(2025, 1, 1, 12, 0, 0),
      LocalDateTime.of(2025, 1, 1, 13, 0, 0),
      LocalDateTime.of(2025, 1, 1, 14, 0, 0),
      LocalDateTime.of(2025, 1, 1, 15, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextIntervalDateTime(
          if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          reminder.repeatInterval
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

