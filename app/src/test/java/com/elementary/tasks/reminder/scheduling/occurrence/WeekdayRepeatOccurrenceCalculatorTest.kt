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
 * Unit tests for WeekdayRepeatOccurrenceCalculator.
 *
 * Tests the calculator for weekday-based repeating reminders.
 * Validates occurrence generation for various weekday combinations.
 */
class WeekdayRepeatOccurrenceCalculatorTest {

  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: WeekdayRepeatOccurrenceCalculator

  @Before
  fun setup() {
    recurrenceCalculator = mockk()
    calculator = WeekdayRepeatOccurrenceCalculator(recurrenceCalculator)
  }

  @Test
  fun `calculateOccurrences should generate occurrences for selected weekdays`() = runTest {
    // Arrange - Monday and Wednesday
    val reminder = Reminder(
      summary = "Weekday reminder",
      weekdays = listOf(0, 1, 0, 1, 0, 0, 0), // Monday and Wednesday
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 10, 0, 0) // Monday
    val numberOfOccurrences = 4

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 8, 10, 0, 0),  // Wednesday
      LocalDateTime.of(2025, 1, 13, 10, 0, 0), // Monday
      LocalDateTime.of(2025, 1, 15, 10, 0, 0), // Wednesday
      LocalDateTime.of(2025, 1, 20, 10, 0, 0)  // Monday
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextDayOfWeekDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          weekdays = reminder.weekdays
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
    assertEquals(expectedOccurrences, result)
  }

  @Test
  fun `calculateOccurrences should respect repeat limit`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Limited weekday reminder",
      weekdays = listOf(0, 1, 1, 1, 1, 1, 0), // Monday to Friday
      repeatLimit = 15,
      eventCount = 12L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = 10

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 7, 9, 0, 0),
      LocalDateTime.of(2025, 1, 8, 9, 0, 0),
      LocalDateTime.of(2025, 1, 9, 9, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextDayOfWeekDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          weekdays = reminder.weekdays
        )
      } returns occurrence
    }

    // Act - Should only generate 3 occurrences (15 limit - 12 already occurred)
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
      summary = "Exhausted weekday reminder",
      weekdays = listOf(0, 1, 1, 1, 1, 1, 0),
      repeatLimit = 20,
      eventCount = 20L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
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
  fun `calculateOccurrences should return empty list for empty weekdays`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "No weekdays reminder",
      weekdays = emptyList(),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
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
      summary = "Weekday reminder",
      weekdays = listOf(0, 1, 0, 1, 0, 0, 0),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
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
      summary = "Weekday reminder",
      weekdays = listOf(0, 1, 0, 1, 0, 0, 0),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
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
  fun `calculateOccurrences should handle weekend only reminder`() = runTest {
    // Arrange - Saturday and Sunday only
    val reminder = Reminder(
      summary = "Weekend reminder",
      weekdays = listOf(1, 0, 0, 0, 0, 0, 1), // Sunday and Saturday
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 10, 10, 0, 0) // Friday
    val numberOfOccurrences = 4

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 11, 10, 0, 0), // Saturday
      LocalDateTime.of(2025, 1, 12, 10, 0, 0), // Sunday
      LocalDateTime.of(2025, 1, 18, 10, 0, 0), // Saturday
      LocalDateTime.of(2025, 1, 19, 10, 0, 0)  // Sunday
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextDayOfWeekDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          weekdays = reminder.weekdays
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
  fun `calculateOccurrences should handle every day reminder`() = runTest {
    // Arrange - All days selected
    val reminder = Reminder(
      summary = "Daily reminder",
      weekdays = listOf(1, 1, 1, 1, 1, 1, 1),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 8, 30, 0)
    val numberOfOccurrences = 7

    val expectedOccurrences = (1..7).map {
      LocalDateTime.of(2025, 1, 6 + it, 8, 30, 0)
    }

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextDayOfWeekDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          weekdays = reminder.weekdays
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
    assertEquals(7, result.size)
  }

  @Test
  fun `calculateOccurrences should work with unlimited reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Unlimited weekday reminder",
      weekdays = listOf(0, 1, 0, 0, 0, 0, 0), // Monday only
      repeatLimit = -1,
      eventCount = 50L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = 5

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 13, 9, 0, 0),
      LocalDateTime.of(2025, 1, 20, 9, 0, 0),
      LocalDateTime.of(2025, 1, 27, 9, 0, 0),
      LocalDateTime.of(2025, 2, 3, 9, 0, 0),
      LocalDateTime.of(2025, 2, 10, 9, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextDayOfWeekDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          weekdays = reminder.weekdays
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

