package com.elementary.tasks.reminder.scheduling.occurrence

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.common.datetime.DateTimeManager
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
import org.threeten.bp.LocalTime

/**
 * Unit tests for TimerRepeatOccurrenceCalculator.
 *
 * Tests the calculator for timer-based repeating reminders.
 * Validates occurrence generation with time windows and excluded hours.
 */
class TimerRepeatOccurrenceCalculatorTest {

  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var calculator: TimerRepeatOccurrenceCalculator

  @Before
  fun setup() {
    dateTimeManager = mockk()
    recurrenceCalculator = mockk()
    calculator = TimerRepeatOccurrenceCalculator(dateTimeManager, recurrenceCalculator)
  }

  @Test
  fun `calculateOccurrences should generate timer occurrences`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Timer reminder",
      repeatInterval = 3600000L, // 1 hour
      from = "09:00",
      to = "17:00",
      hours = listOf(12, 13), // Exclude lunch hour
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = 5

    val fromTime = LocalTime.of(9, 0, 0)
    val toTime = LocalTime.of(17, 0, 0)

    every { dateTimeManager.toLocalTime(reminder.from) } returns fromTime
    every { dateTimeManager.toLocalTime(reminder.to) } returns toTime

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 6, 10, 0, 0),
      LocalDateTime.of(2025, 1, 6, 11, 0, 0),
      LocalDateTime.of(2025, 1, 6, 14, 0, 0), // Skips 12-13
      LocalDateTime.of(2025, 1, 6, 15, 0, 0),
      LocalDateTime.of(2025, 1, 6, 16, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextTimerDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          interval = reminder.repeatInterval,
          excludedHours = reminder.hours,
          excludedFromTime = fromTime,
          excludedToTime = toTime
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
      summary = "Limited timer reminder",
      repeatInterval = 1800000L, // 30 minutes
      from = "08:00",
      to = "18:00",
      hours = emptyList(),
      repeatLimit = 20,
      eventCount = 17L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 8, 0, 0)
    val numberOfOccurrences = 10

    val fromTime = LocalTime.of(8, 0, 0)
    val toTime = LocalTime.of(18, 0, 0)

    every { dateTimeManager.toLocalTime(reminder.from) } returns fromTime
    every { dateTimeManager.toLocalTime(reminder.to) } returns toTime

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 6, 8, 30, 0),
      LocalDateTime.of(2025, 1, 6, 9, 0, 0),
      LocalDateTime.of(2025, 1, 6, 9, 30, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextTimerDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          interval = reminder.repeatInterval,
          excludedHours = reminder.hours,
          excludedFromTime = fromTime,
          excludedToTime = toTime
        )
      } returns occurrence
    }

    // Act - Should only generate 3 occurrences (20 limit - 17 already occurred)
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
      summary = "Exhausted timer reminder",
      repeatInterval = 3600000L,
      from = "09:00",
      to = "17:00",
      hours = emptyList(),
      repeatLimit = 50,
      eventCount = 50L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = 5

    every { dateTimeManager.toLocalTime(reminder.from) } returns LocalTime.of(9, 0, 0)
    every { dateTimeManager.toLocalTime(reminder.to) } returns LocalTime.of(17, 0, 0)

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
      summary = "Timer reminder",
      repeatInterval = 3600000L,
      from = "09:00",
      to = "17:00",
      hours = emptyList(),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = 0

    every { dateTimeManager.toLocalTime(reminder.from) } returns LocalTime.of(9, 0, 0)
    every { dateTimeManager.toLocalTime(reminder.to) } returns LocalTime.of(17, 0, 0)

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
      summary = "Timer reminder",
      repeatInterval = 3600000L,
      from = "09:00",
      to = "17:00",
      hours = emptyList(),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = -5

    every { dateTimeManager.toLocalTime(reminder.from) } returns LocalTime.of(9, 0, 0)
    every { dateTimeManager.toLocalTime(reminder.to) } returns LocalTime.of(17, 0, 0)

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
      summary = "Invalid timer reminder",
      repeatInterval = 0L,
      from = "09:00",
      to = "17:00",
      hours = emptyList(),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = 5

    every { dateTimeManager.toLocalTime(reminder.from) } returns LocalTime.of(9, 0, 0)
    every { dateTimeManager.toLocalTime(reminder.to) } returns LocalTime.of(17, 0, 0)

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
      summary = "Invalid timer reminder",
      repeatInterval = -3600000L,
      from = "09:00",
      to = "17:00",
      hours = emptyList(),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = 5

    every { dateTimeManager.toLocalTime(reminder.from) } returns LocalTime.of(9, 0, 0)
    every { dateTimeManager.toLocalTime(reminder.to) } returns LocalTime.of(17, 0, 0)

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
  fun `calculateOccurrences should handle overnight time window`() = runTest {
    // Arrange - Timer runs overnight
    val reminder = Reminder(
      summary = "Overnight timer reminder",
      repeatInterval = 7200000L, // 2 hours
      from = "22:00",
      to = "06:00",
      hours = emptyList(),
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 22, 0, 0)
    val numberOfOccurrences = 3

    val fromTime = LocalTime.of(22, 0, 0)
    val toTime = LocalTime.of(6, 0, 0)

    every { dateTimeManager.toLocalTime(reminder.from) } returns fromTime
    every { dateTimeManager.toLocalTime(reminder.to) } returns toTime

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 7, 0, 0, 0),
      LocalDateTime.of(2025, 1, 7, 2, 0, 0),
      LocalDateTime.of(2025, 1, 7, 4, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextTimerDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          interval = reminder.repeatInterval,
          excludedHours = reminder.hours,
          excludedFromTime = fromTime,
          excludedToTime = toTime
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
  fun `calculateOccurrences should work with unlimited reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Unlimited timer reminder",
      repeatInterval = 3600000L,
      from = "09:00",
      to = "17:00",
      hours = emptyList(),
      repeatLimit = -1,
      eventCount = 100L,
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 9, 0, 0)
    val numberOfOccurrences = 5

    val fromTime = LocalTime.of(9, 0, 0)
    val toTime = LocalTime.of(17, 0, 0)

    every { dateTimeManager.toLocalTime(reminder.from) } returns fromTime
    every { dateTimeManager.toLocalTime(reminder.to) } returns toTime

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 6, 10, 0, 0),
      LocalDateTime.of(2025, 1, 6, 11, 0, 0),
      LocalDateTime.of(2025, 1, 6, 12, 0, 0),
      LocalDateTime.of(2025, 1, 6, 13, 0, 0),
      LocalDateTime.of(2025, 1, 6, 14, 0, 0)
    )

    expectedOccurrences.forEachIndexed { index, occurrence ->
      every {
        recurrenceCalculator.getNextTimerDateTime(
          eventDateTime = if (index == 0) fromDateTime else expectedOccurrences[index - 1],
          interval = reminder.repeatInterval,
          excludedHours = reminder.hours,
          excludedFromTime = fromTime,
          excludedToTime = toTime
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

