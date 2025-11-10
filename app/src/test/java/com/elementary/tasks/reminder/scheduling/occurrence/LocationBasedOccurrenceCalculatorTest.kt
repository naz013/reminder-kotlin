package com.elementary.tasks.reminder.scheduling.occurrence

import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for LocationBasedOccurrenceCalculator.
 *
 * Tests the calculator for location-based reminders.
 * Since these are triggered by geofence events, they have no time-based occurrences.
 */
class LocationBasedOccurrenceCalculatorTest {

  private lateinit var calculator: LocationBasedOccurrenceCalculator

  @Before
  fun setup() {
    calculator = LocationBasedOccurrenceCalculator()
  }

  @Test
  fun `calculateOccurrences should return empty list for location-based reminder`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Location reminder",
      places = listOf(
        Place(
          latitude = 40.7128,
          longitude = -74.0060,
          name = "Office",
          syncState = SyncState.Synced
        )
      ),
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
  fun `calculateOccurrences should return empty list with multiple places`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Multiple locations reminder",
      places = listOf(
        Place(
          latitude = 40.7128,
          longitude = -74.0060,
          name = "Office",
          syncState = SyncState.Synced
        ),
        Place(
          latitude = 51.5074,
          longitude = -0.1278,
          name = "Home",
          syncState = SyncState.Synced
        )
      ),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
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

  @Test
  fun `calculateOccurrences should return empty list with zero occurrences requested`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Location reminder",
      places = listOf(
        Place(
          latitude = 40.7128,
          longitude = -74.0060,
          name = "Office",
          syncState = SyncState.Synced
        )
      ),
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
      summary = "Location reminder",
      places = listOf(
        Place(
          latitude = 40.7128,
          longitude = -74.0060,
          name = "Office",
          syncState = SyncState.Synced
        )
      ),
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
}

