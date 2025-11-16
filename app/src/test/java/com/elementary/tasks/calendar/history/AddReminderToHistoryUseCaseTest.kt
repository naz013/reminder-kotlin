package com.elementary.tasks.calendar.history

import com.elementary.tasks.BaseTest
import com.elementary.tasks.reminder.scheduling.behavior.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.behavior.LocationBasedStrategy
import com.elementary.tasks.reminder.scheduling.behavior.SimpleDateStrategy
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.EventHistoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Unit tests for AddReminderToHistoryUseCase.
 *
 * Tests the behavior of adding reminders to event history, covering both
 * location-based reminders and time-based reminders with various scenarios.
 */
class AddReminderToHistoryUseCaseTest : BaseTest() {

  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var strategyResolver: BehaviorStrategyResolver
  private lateinit var eventHistoryRepository: EventHistoryRepository
  private lateinit var useCase: AddReminderToHistoryUseCase

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    strategyResolver = mockk()
    eventHistoryRepository = mockk(relaxed = true)
    useCase = AddReminderToHistoryUseCase(
      dateTimeManager = dateTimeManager,
      strategyResolver = strategyResolver,
      eventHistoryRepository = eventHistoryRepository
    )
  }

  @Test
  fun `invoke saves location-based reminder with current datetime`() = runTest {
    // Arrange
    val reminderId = "reminder-location-123"
    val currentDateTime = LocalDateTime.of(2025, 11, 16, 14, 30)
    val reminder = Reminder(
      uuId = reminderId,
      summary = "Reminder at office",
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
    val locationStrategy = mockk<LocationBasedStrategy>()
    val recordSlot = slot<EventHistoricalRecord>()

    every { strategyResolver.resolve(reminder) } returns locationStrategy
    every { dateTimeManager.getCurrentDateTime() } returns currentDateTime
    coEvery { eventHistoryRepository.save(capture(recordSlot)) } returns Unit

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 1) { eventHistoryRepository.save(any()) }
    val savedRecord = recordSlot.captured
    assertEquals(reminderId, savedRecord.eventId)
    assertEquals(EventHistoricalRecordType.Reminder, savedRecord.type)
    assertEquals(LocalDate.of(2025, 11, 16), savedRecord.date)
    assertEquals(LocalTime.of(14, 30), savedRecord.time)
    assertNotNull(savedRecord.id)
  }

  @Test
  fun `invoke saves time-based reminder with converted event time`() = runTest {
    // Arrange
    val reminderId = "reminder-time-456"
    val eventTimeGmt = "2025-11-20T18:00:00"
    val convertedDateTime = LocalDateTime.of(2025, 11, 20, 20, 0)
    val reminder = Reminder(
      uuId = reminderId,
      summary = "Meeting reminder",
      eventTime = eventTimeGmt,
      syncState = SyncState.Synced
    )
    val dateStrategy = mockk<SimpleDateStrategy>()
    val recordSlot = slot<EventHistoricalRecord>()

    every { strategyResolver.resolve(reminder) } returns dateStrategy
    every { dateTimeManager.fromGmtToLocal(eventTimeGmt) } returns convertedDateTime
    coEvery { eventHistoryRepository.save(capture(recordSlot)) } returns Unit

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 1) { eventHistoryRepository.save(any()) }
    val savedRecord = recordSlot.captured
    assertEquals(reminderId, savedRecord.eventId)
    assertEquals(EventHistoricalRecordType.Reminder, savedRecord.type)
    assertEquals(LocalDate.of(2025, 11, 20), savedRecord.date)
    assertEquals(LocalTime.of(20, 0), savedRecord.time)
    assertNotNull(savedRecord.id)
  }

  @Test
  fun `invoke does not save when time conversion fails for time-based reminder`() = runTest {
    // Arrange
    val reminderId = "reminder-invalid-time-789"
    val invalidEventTime = "invalid-time-format"
    val reminder = Reminder(
      uuId = reminderId,
      summary = "Reminder with invalid time",
      eventTime = invalidEventTime,
      syncState = SyncState.Synced
    )
    val dateStrategy = mockk<SimpleDateStrategy>()

    every { strategyResolver.resolve(reminder) } returns dateStrategy
    every { dateTimeManager.fromGmtToLocal(invalidEventTime) } returns null

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 0) { eventHistoryRepository.save(any()) }
  }

  @Test
  fun `invoke handles reminder with empty event time gracefully`() = runTest {
    // Arrange
    val reminderId = "reminder-empty-time-101"
    val reminder = Reminder(
      uuId = reminderId,
      summary = "Reminder with empty time",
      eventTime = "",
      syncState = SyncState.Synced
    )
    val dateStrategy = mockk<SimpleDateStrategy>()

    every { strategyResolver.resolve(reminder) } returns dateStrategy
    every { dateTimeManager.fromGmtToLocal("") } returns null

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 0) { eventHistoryRepository.save(any()) }
  }

  @Test
  fun `invoke saves location-based reminder with midnight time`() = runTest {
    // Arrange
    val reminderId = "reminder-midnight-202"
    val midnightDateTime = LocalDateTime.of(2025, 11, 16, 0, 0, 0)
    val reminder = Reminder(
      uuId = reminderId,
      summary = "Midnight location reminder",
      places = listOf(
        Place(
          latitude = 51.5074,
          longitude = -0.1278,
          name = "London Office",
          syncState = SyncState.Synced
        )
      ),
      syncState = SyncState.Synced
    )
    val locationStrategy = mockk<LocationBasedStrategy>()
    val recordSlot = slot<EventHistoricalRecord>()

    every { strategyResolver.resolve(reminder) } returns locationStrategy
    every { dateTimeManager.getCurrentDateTime() } returns midnightDateTime
    coEvery { eventHistoryRepository.save(capture(recordSlot)) } returns Unit

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 1) { eventHistoryRepository.save(any()) }
    val savedRecord = recordSlot.captured
    assertEquals(LocalTime.of(0, 0, 0), savedRecord.time)
  }

  @Test
  fun `invoke saves reminder with very long summary text`() = runTest {
    // Arrange
    val reminderId = "reminder-long-text-303"
    val longSummary = "This is a very long reminder summary that contains a lot of text " +
      "to test edge cases where the reminder description might be unusually long and " +
      "could potentially cause issues with database storage or processing. " +
      "The system should handle this gracefully without errors."
    val currentDateTime = LocalDateTime.of(2025, 11, 16, 10, 15)
    val reminder = Reminder(
      uuId = reminderId,
      summary = longSummary,
      places = listOf(
        Place(
          latitude = 34.0522,
          longitude = -118.2437,
          name = "LA Office",
          syncState = SyncState.Synced
        )
      ),
      syncState = SyncState.Synced
    )
    val locationStrategy = mockk<LocationBasedStrategy>()
    val recordSlot = slot<EventHistoricalRecord>()

    every { strategyResolver.resolve(reminder) } returns locationStrategy
    every { dateTimeManager.getCurrentDateTime() } returns currentDateTime
    coEvery { eventHistoryRepository.save(capture(recordSlot)) } returns Unit

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 1) { eventHistoryRepository.save(any()) }
    val savedRecord = recordSlot.captured
    assertEquals(reminderId, savedRecord.eventId)
  }

  @Test
  fun `invoke saves time-based reminder at end of year boundary`() = runTest {
    // Arrange
    val reminderId = "reminder-year-end-404"
    val eventTimeGmt = "2025-12-31T23:59:00"
    val convertedDateTime = LocalDateTime.of(2025, 12, 31, 23, 59)
    val reminder = Reminder(
      uuId = reminderId,
      summary = "New Year's Eve reminder",
      eventTime = eventTimeGmt,
      syncState = SyncState.Synced
    )
    val dateStrategy = mockk<SimpleDateStrategy>()
    val recordSlot = slot<EventHistoricalRecord>()

    every { strategyResolver.resolve(reminder) } returns dateStrategy
    every { dateTimeManager.fromGmtToLocal(eventTimeGmt) } returns convertedDateTime
    coEvery { eventHistoryRepository.save(capture(recordSlot)) } returns Unit

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 1) { eventHistoryRepository.save(any()) }
    val savedRecord = recordSlot.captured
    assertEquals(LocalDate.of(2025, 12, 31), savedRecord.date)
    assertEquals(LocalTime.of(23, 59), savedRecord.time)
  }

  @Test
  fun `invoke generates unique IDs for multiple reminders`() = runTest {
    // Arrange
    val reminder1 = Reminder(
      uuId = "reminder-unique-1",
      summary = "First reminder",
      places = listOf(
        Place(
          latitude = 40.7128,
          longitude = -74.0060,
          name = "Office 1",
          syncState = SyncState.Synced
        )
      ),
      syncState = SyncState.Synced
    )
    val reminder2 = Reminder(
      uuId = "reminder-unique-2",
      summary = "Second reminder",
      places = listOf(
        Place(
          latitude = 51.5074,
          longitude = -0.1278,
          name = "Office 2",
          syncState = SyncState.Synced
        )
      ),
      syncState = SyncState.Synced
    )
    val currentDateTime = LocalDateTime.of(2025, 11, 16, 12, 0)
    val locationStrategy = mockk<LocationBasedStrategy>()
    val recordSlots = mutableListOf<EventHistoricalRecord>()

    every { strategyResolver.resolve(any()) } returns locationStrategy
    every { dateTimeManager.getCurrentDateTime() } returns currentDateTime
    coEvery { eventHistoryRepository.save(capture(recordSlots)) } returns Unit

    // Act
    useCase.invoke(reminder1)
    useCase.invoke(reminder2)

    // Assert
    coVerify(exactly = 2) { eventHistoryRepository.save(any()) }
    assertEquals(2, recordSlots.size)
    // Verify that IDs are different
    assertEquals(false, recordSlots[0].id == recordSlots[1].id)
    assertEquals("reminder-unique-1", recordSlots[0].eventId)
    assertEquals("reminder-unique-2", recordSlots[1].eventId)
  }

  @Test
  fun `invoke correctly handles timezone conversion with positive offset`() = runTest {
    // Arrange
    val reminderId = "reminder-timezone-505"
    val eventTimeGmt = "2025-11-16T12:00:00" // Noon GMT
    val convertedDateTime = LocalDateTime.of(2025, 11, 16, 17, 0) // 5 PM local (GMT+5)
    val reminder = Reminder(
      uuId = reminderId,
      summary = "Timezone test reminder",
      eventTime = eventTimeGmt,
      syncState = SyncState.Synced
    )
    val dateStrategy = mockk<SimpleDateStrategy>()
    val recordSlot = slot<EventHistoricalRecord>()

    every { strategyResolver.resolve(reminder) } returns dateStrategy
    every { dateTimeManager.fromGmtToLocal(eventTimeGmt) } returns convertedDateTime
    coEvery { eventHistoryRepository.save(capture(recordSlot)) } returns Unit

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 1) { eventHistoryRepository.save(any()) }
    val savedRecord = recordSlot.captured
    assertEquals(LocalTime.of(17, 0), savedRecord.time)
  }

  @Test
  fun `invoke resolves correct strategy type and uses appropriate datetime source`() = runTest {
    // Arrange - Location-based reminder
    val locationReminder = Reminder(
      uuId = "reminder-strategy-606",
      summary = "Strategy test",
      places = listOf(
        Place(
          latitude = 35.6762,
          longitude = 139.6503,
          name = "Tokyo Office",
          syncState = SyncState.Synced
        )
      ),
      syncState = SyncState.Synced
    )
    val locationStrategy = mockk<LocationBasedStrategy>()
    val currentDateTime = LocalDateTime.of(2025, 11, 16, 9, 30)

    every { strategyResolver.resolve(locationReminder) } returns locationStrategy
    every { dateTimeManager.getCurrentDateTime() } returns currentDateTime
    coEvery { eventHistoryRepository.save(any()) } returns Unit

    // Act
    useCase.invoke(locationReminder)

    // Assert
    // Verify getCurrentDateTime was called for location-based strategy
    coVerify(exactly = 1) { dateTimeManager.getCurrentDateTime() }
    coVerify(exactly = 0) { dateTimeManager.fromGmtToLocal(any()) }
  }
}
