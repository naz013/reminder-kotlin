package com.elementary.tasks.reminder.scheduling.occurrence

import com.elementary.tasks.core.utils.datetime.RecurEventManager
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
 * Unit tests for RecurRepeatOccurrenceCalculator.
 *
 * Tests the calculator for RRULE-based repeating reminders.
 * Validates occurrence generation using iCalendar recurrence rules.
 */
class RecurRepeatOccurrenceCalculatorTest {

  private lateinit var recurEventManager: RecurEventManager
  private lateinit var calculator: RecurRepeatOccurrenceCalculator

  @Before
  fun setup() {
    recurEventManager = mockk()
    calculator = RecurRepeatOccurrenceCalculator(recurEventManager)
  }

  @Test
  fun `calculateOccurrences should generate occurrences from RRULE`() = runTest {
    // Arrange - Weekly on Tuesday and Thursday
    val reminder = Reminder(
      summary = "RRULE reminder",
      recurDataObject = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 10, 0, 0)
    val numberOfOccurrences = 4

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 7, 10, 0, 0),  // Tuesday
      LocalDateTime.of(2025, 1, 9, 10, 0, 0),  // Thursday
      LocalDateTime.of(2025, 1, 14, 10, 0, 0), // Tuesday
      LocalDateTime.of(2025, 1, 16, 10, 0, 0)  // Thursday
    )

    // Mock each call to getNextAfterDateTime
    every {
      recurEventManager.getNextAfterDateTime(fromDateTime, reminder.recurDataObject!!)
    } returns expectedOccurrences[0]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[0].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[1]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[1].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[2]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[2].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[3]

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
  fun `calculateOccurrences should handle limited RRULE with COUNT`() = runTest {
    // Arrange - Daily for 5 days
    val reminder = Reminder(
      summary = "Limited RRULE",
      recurDataObject = "RRULE:FREQ=DAILY;COUNT=5",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
    val numberOfOccurrences = 10

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 2, 9, 0, 0),
      LocalDateTime.of(2025, 1, 3, 9, 0, 0),
      LocalDateTime.of(2025, 1, 4, 9, 0, 0)
    )

    every {
      recurEventManager.getNextAfterDateTime(fromDateTime, reminder.recurDataObject!!)
    } returns expectedOccurrences[0]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[0].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[1]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[1].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[2]

    // After 3 occurrences, COUNT limit reached
    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[2].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns null

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert - Should only return 3 occurrences even though 10 were requested
    assertEquals(3, result.size)
  }

  @Test
  fun `calculateOccurrences should handle RRULE with UNTIL date`() = runTest {
    // Arrange - Weekly until a specific date
    val reminder = Reminder(
      summary = "UNTIL RRULE",
      recurDataObject = "RRULE:FREQ=WEEKLY;UNTIL=20250131T000000Z",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 6, 10, 0, 0)
    val numberOfOccurrences = 10

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 13, 10, 0, 0),
      LocalDateTime.of(2025, 1, 20, 10, 0, 0),
      LocalDateTime.of(2025, 1, 27, 10, 0, 0)
    )

    every {
      recurEventManager.getNextAfterDateTime(fromDateTime, reminder.recurDataObject!!)
    } returns expectedOccurrences[0]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[0].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[1]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[1].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[2]

    // After Jan 27, UNTIL date prevents more occurrences
    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[2].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns null

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
  fun `calculateOccurrences should return empty list for empty recurDataObject`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "No RRULE",
      recurDataObject = "",
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
  fun `calculateOccurrences should return empty list for null recurDataObject`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "No RRULE",
      recurDataObject = null,
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
      summary = "RRULE reminder",
      recurDataObject = "RRULE:FREQ=DAILY",
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
      summary = "RRULE reminder",
      recurDataObject = "RRULE:FREQ=DAILY",
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
  fun `calculateOccurrences should handle monthly RRULE`() = runTest {
    // Arrange - Monthly on the 15th
    val reminder = Reminder(
      summary = "Monthly RRULE",
      recurDataObject = "RRULE:FREQ=MONTHLY;BYMONTHDAY=15",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 3

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 15, 10, 0, 0),
      LocalDateTime.of(2025, 2, 15, 10, 0, 0),
      LocalDateTime.of(2025, 3, 15, 10, 0, 0)
    )

    every {
      recurEventManager.getNextAfterDateTime(fromDateTime, reminder.recurDataObject!!)
    } returns expectedOccurrences[0]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[0].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[1]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[1].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[2]

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
  fun `calculateOccurrences should handle complex RRULE pattern`() = runTest {
    // Arrange - Second Tuesday of every month
    val reminder = Reminder(
      summary = "Complex RRULE",
      recurDataObject = "RRULE:FREQ=MONTHLY;BYDAY=2TU",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 14, 0, 0)
    val numberOfOccurrences = 4

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 14, 14, 0, 0), // 2nd Tuesday of January
      LocalDateTime.of(2025, 2, 11, 14, 0, 0), // 2nd Tuesday of February
      LocalDateTime.of(2025, 3, 11, 14, 0, 0), // 2nd Tuesday of March
      LocalDateTime.of(2025, 4, 8, 14, 0, 0)   // 2nd Tuesday of April
    )

    every {
      recurEventManager.getNextAfterDateTime(fromDateTime, reminder.recurDataObject!!)
    } returns expectedOccurrences[0]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[0].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[1]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[1].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[2]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[2].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[3]

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
  fun `calculateOccurrences should stop early when RRULE ends`() = runTest {
    // Arrange - Only 2 occurrences available
    val reminder = Reminder(
      summary = "Short RRULE",
      recurDataObject = "RRULE:FREQ=DAILY;COUNT=2",
      syncState = SyncState.Synced
    )
    val fromDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
    val numberOfOccurrences = 10

    val expectedOccurrences = listOf(
      LocalDateTime.of(2025, 1, 2, 10, 0, 0),
      LocalDateTime.of(2025, 1, 3, 10, 0, 0)
    )

    every {
      recurEventManager.getNextAfterDateTime(fromDateTime, reminder.recurDataObject!!)
    } returns expectedOccurrences[0]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[0].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns expectedOccurrences[1]

    every {
      recurEventManager.getNextAfterDateTime(
        expectedOccurrences[1].plusSeconds(1),
        reminder.recurDataObject!!
      )
    } returns null

    // Act
    val result = calculator.calculateOccurrences(
      reminder,
      fromDateTime,
      numberOfOccurrences
    )

    // Assert - Should return only 2 even though 10 were requested
    assertEquals(2, result.size)
  }
}

