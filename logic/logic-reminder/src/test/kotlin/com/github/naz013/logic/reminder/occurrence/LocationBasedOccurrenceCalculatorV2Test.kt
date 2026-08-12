package com.github.naz013.logic.reminder.occurrence

import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for [LocationBasedOccurrenceCalculatorV2], mirroring
 * `LocationBasedOccurrenceCalculatorTest` (the V1 equivalent).
 */
class LocationBasedOccurrenceCalculatorV2Test {
  private lateinit var calculator: LocationBasedOccurrenceCalculatorV2

  @Before
  fun setup() {
    calculator = LocationBasedOccurrenceCalculatorV2()
  }

  private fun place(name: String) =
    Place(latitude = 40.7128, longitude = -74.0060, name = name, syncState = SyncState.Synced)

  private fun reminder(places: List<Place>) =
    ReminderV2(
      places = places,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )

  @Test
  fun `calculateOccurrences should return empty list for location-based reminder`() =
    runTest {
      val reminder = reminder(listOf(place("Office")))
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 5)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list with multiple places`() =
    runTest {
      val reminder = reminder(listOf(place("Office"), place("Home")))
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 10)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list with zero occurrences requested`() =
    runTest {
      val reminder = reminder(listOf(place("Office")))
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, 0)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `calculateOccurrences should return empty list with negative occurrences`() =
    runTest {
      val reminder = reminder(listOf(place("Office")))
      val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)

      val result = calculator.calculateOccurrences(reminder, fromDateTime, -5)

      assertTrue(result.isEmpty())
    }
}
