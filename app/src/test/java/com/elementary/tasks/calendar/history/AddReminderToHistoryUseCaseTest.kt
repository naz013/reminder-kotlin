package com.elementary.tasks.calendar.history

import com.elementary.tasks.BaseTest
import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.LocationBasedStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.NoReminderStrategyV2
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
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
  private lateinit var strategyResolver: BehaviorStrategyResolverV2
  private lateinit var eventHistoryRepository: EventHistoryRepository
  private lateinit var useCase: AddReminderToHistoryUseCase

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    strategyResolver = mockk()
    eventHistoryRepository = mockk(relaxed = true)
    useCase =
      AddReminderToHistoryUseCase(
        dateTimeManager = dateTimeManager,
        strategyResolver = strategyResolver,
        eventHistoryRepository = eventHistoryRepository,
      )
  }

  private fun placeReminder(uuId: String, summary: String, name: String = "Office") =
    ReminderV2(
      uuId = uuId,
      summary = summary,
      places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = name, syncState = SyncState.Synced)),
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )

  private fun timeReminder(uuId: String, summary: String, eventDateTime: LocalDateTime?) =
    ReminderV2(
      uuId = uuId,
      summary = summary,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now(), eventDateTime = eventDateTime),
    )

  @Test
  fun `invoke saves location-based reminder with current datetime`() =
    runTest {
      // Arrange
      val reminderId = "reminder-location-123"
      val currentDateTime = LocalDateTime.of(2025, 11, 16, 14, 30)
      val reminder = placeReminder(reminderId, "Reminder at office")
      val recordSlot = slot<EventHistoricalRecord>()

      every { strategyResolver.resolve(reminder) } returns LocationBasedStrategyV2
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
  fun `invoke saves time-based reminder with converted event time`() =
    runTest {
      // Arrange
      val reminderId = "reminder-time-456"
      val eventDateTime = LocalDateTime.of(2025, 11, 20, 18, 0, 0)
      val convertedDateTime = LocalDateTime.of(2025, 11, 20, 20, 0)
      val reminder = timeReminder(reminderId, "Meeting reminder", eventDateTime)
      val recordSlot = slot<EventHistoricalRecord>()

      every { strategyResolver.resolve(reminder) } returns NoReminderStrategyV2
      every { dateTimeManager.utcToLocal(eventDateTime) } returns convertedDateTime
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
  fun `invoke does not save when time-based reminder has no event time`() =
    runTest {
      // Arrange
      val reminder = timeReminder("reminder-no-time-789", "Reminder with no event time", null)

      every { strategyResolver.resolve(reminder) } returns NoReminderStrategyV2

      // Act
      useCase.invoke(reminder)

      // Assert
      coVerify(exactly = 0) { eventHistoryRepository.save(any()) }
    }

  @Test
  fun `invoke saves location-based reminder with midnight time`() =
    runTest {
      // Arrange
      val reminderId = "reminder-midnight-202"
      val midnightDateTime = LocalDateTime.of(2025, 11, 16, 0, 0, 0)
      val reminder = placeReminder(reminderId, "Midnight location reminder", "London Office")
      val recordSlot = slot<EventHistoricalRecord>()

      every { strategyResolver.resolve(reminder) } returns LocationBasedStrategyV2
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
  fun `invoke saves reminder with very long summary text`() =
    runTest {
      // Arrange
      val reminderId = "reminder-long-text-303"
      val longSummary =
        "This is a very long reminder summary that contains a lot of text " +
          "to test edge cases where the reminder description might be unusually long and " +
          "could potentially cause issues with database storage or processing. " +
          "The system should handle this gracefully without errors."
      val currentDateTime = LocalDateTime.of(2025, 11, 16, 10, 15)
      val reminder = placeReminder(reminderId, longSummary, "LA Office")
      val recordSlot = slot<EventHistoricalRecord>()

      every { strategyResolver.resolve(reminder) } returns LocationBasedStrategyV2
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
  fun `invoke saves time-based reminder at end of year boundary`() =
    runTest {
      // Arrange
      val reminderId = "reminder-year-end-404"
      val eventDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 0)
      val convertedDateTime = LocalDateTime.of(2025, 12, 31, 23, 59)
      val reminder = timeReminder(reminderId, "New Year's Eve reminder", eventDateTime)
      val recordSlot = slot<EventHistoricalRecord>()

      every { strategyResolver.resolve(reminder) } returns NoReminderStrategyV2
      every { dateTimeManager.utcToLocal(eventDateTime) } returns convertedDateTime
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
  fun `invoke generates unique IDs for multiple reminders`() =
    runTest {
      // Arrange
      val reminder1 = placeReminder("reminder-unique-1", "First reminder", "Office 1")
      val reminder2 = placeReminder("reminder-unique-2", "Second reminder", "Office 2")
      val currentDateTime = LocalDateTime.of(2025, 11, 16, 12, 0)
      val recordSlots = mutableListOf<EventHistoricalRecord>()

      every { strategyResolver.resolve(any()) } returns LocationBasedStrategyV2
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
  fun `invoke correctly handles timezone conversion with positive offset`() =
    runTest {
      // Arrange
      val reminderId = "reminder-timezone-505"
      val eventDateTime = LocalDateTime.of(2025, 11, 16, 12, 0, 0) // Noon UTC
      val convertedDateTime = LocalDateTime.of(2025, 11, 16, 17, 0) // 5 PM local (UTC+5)
      val reminder = timeReminder(reminderId, "Timezone test reminder", eventDateTime)
      val recordSlot = slot<EventHistoricalRecord>()

      every { strategyResolver.resolve(reminder) } returns NoReminderStrategyV2
      every { dateTimeManager.utcToLocal(eventDateTime) } returns convertedDateTime
      coEvery { eventHistoryRepository.save(capture(recordSlot)) } returns Unit

      // Act
      useCase.invoke(reminder)

      // Assert
      coVerify(exactly = 1) { eventHistoryRepository.save(any()) }
      val savedRecord = recordSlot.captured
      assertEquals(LocalTime.of(17, 0), savedRecord.time)
    }

  @Test
  fun `invoke resolves correct strategy type and uses appropriate datetime source`() =
    runTest {
      // Arrange - Location-based reminder
      val locationReminder = placeReminder("reminder-strategy-606", "Strategy test", "Tokyo Office")
      val currentDateTime = LocalDateTime.of(2025, 11, 16, 9, 30)

      every { strategyResolver.resolve(locationReminder) } returns LocationBasedStrategyV2
      every { dateTimeManager.getCurrentDateTime() } returns currentDateTime
      coEvery { eventHistoryRepository.save(any()) } returns Unit

      // Act
      useCase.invoke(locationReminder)

      // Assert
      // Verify getCurrentDateTime was called for location-based strategy
      coVerify(exactly = 1) { dateTimeManager.getCurrentDateTime() }
      coVerify(exactly = 0) { dateTimeManager.utcToLocal(any()) }
    }
}
