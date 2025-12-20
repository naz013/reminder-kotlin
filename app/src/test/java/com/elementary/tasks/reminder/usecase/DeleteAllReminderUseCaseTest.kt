package com.elementary.tasks.reminder.usecase

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.scheduling.usecase.DeactivateReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.sync.DataType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteAllReminderUseCase.
 *
 * Tests the bulk deletion behavior of multiple reminders including
 * deactivation, repository cleanup, calendar sync, and background work scheduling.
 */
class DeleteAllReminderUseCaseTest : BaseTest() {

  private lateinit var reminderRepository: ReminderRepository
  private lateinit var googleCalendarUtils: GoogleCalendarUtils
  private lateinit var scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
  private lateinit var deactivateReminderUseCase: DeactivateReminderUseCase
  private lateinit var eventOccurrenceRepository: EventOccurrenceRepository
  private lateinit var eventHistoryRepository: EventHistoryRepository

  private lateinit var useCase: DeleteAllReminderUseCase

  @Before
  override fun setUp() {
    super.setUp()
    reminderRepository = mockk(relaxed = true)
    googleCalendarUtils = mockk(relaxed = true)
    scheduleBackgroundWorkUseCase = mockk(relaxed = true)
    deactivateReminderUseCase = mockk(relaxed = true)
    eventOccurrenceRepository = mockk(relaxed = true)
    eventHistoryRepository = mockk(relaxed = true)

    useCase = DeleteAllReminderUseCase(
      reminderRepository = reminderRepository,
      googleCalendarUtils = googleCalendarUtils,
      scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
      deactivateReminderUseCase = deactivateReminderUseCase,
      eventOccurrenceRepository = eventOccurrenceRepository,
      eventHistoryRepository = eventHistoryRepository
    )
  }

  @Test
  fun `invoke deactivates all reminders and deletes them from repository`() = runTest {
    // Arrange
    val reminders = listOf(
      Reminder(uuId = "rem-001", summary = "Meeting", syncState = SyncState.Synced),
      Reminder(uuId = "rem-002", summary = "Call", syncState = SyncState.Synced),
      Reminder(uuId = "rem-003", summary = "Task", syncState = SyncState.Synced)
    )

    // Act
    useCase.invoke(reminders)

    // Assert
    coVerify(exactly = 1) { deactivateReminderUseCase(reminders[0]) }
    coVerify(exactly = 1) { deactivateReminderUseCase(reminders[1]) }
    coVerify(exactly = 1) { deactivateReminderUseCase(reminders[2]) }
    coVerify(exactly = 1) { reminderRepository.deleteAll(listOf("rem-001", "rem-002", "rem-003")) }
  }

  @Test
  fun `invoke schedules background delete work with all reminder IDs`() = runTest {
    // Arrange
    val reminders = listOf(
      Reminder(uuId = "work-101", summary = "Reminder 1", syncState = SyncState.Synced),
      Reminder(uuId = "work-102", summary = "Reminder 2", syncState = SyncState.Synced)
    )

    // Act
    useCase.invoke(reminders)

    // Assert
    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        ids = listOf("work-101", "work-102")
      )
    }
  }

  @Test
  fun `invoke deletes calendar events and history for each reminder`() = runTest {
    // Arrange
    val reminders = listOf(
      Reminder(uuId = "cal-201", summary = "Calendar reminder 1", syncState = SyncState.Synced),
      Reminder(uuId = "cal-202", summary = "Calendar reminder 2", syncState = SyncState.Synced),
      Reminder(uuId = "cal-203", summary = "Calendar reminder 3", syncState = SyncState.Synced)
    )

    // Act
    useCase.invoke(reminders)

    // Assert
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("cal-201") }
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("cal-202") }
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("cal-203") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("cal-201") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("cal-202") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("cal-203") }
    coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("cal-201") }
    coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("cal-202") }
    coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("cal-203") }
  }

  @Test
  fun `invoke calls operations in correct order`() = runTest {
    // Arrange
    val reminders = listOf(
      Reminder(uuId = "order-301", summary = "Order test 1", syncState = SyncState.Synced),
      Reminder(uuId = "order-302", summary = "Order test 2", syncState = SyncState.Synced)
    )

    // Act
    useCase.invoke(reminders)

    // Assert - Verify operations happen in the expected sequence
    coVerifyOrder {
      // First: deactivate all reminders
      deactivateReminderUseCase(reminders[0])
      deactivateReminderUseCase(reminders[1])

      // Second: delete from repository
      reminderRepository.deleteAll(listOf("order-301", "order-302"))

      // Third: schedule background work
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        ids = listOf("order-301", "order-302")
      )

      // Fourth: cleanup calendar and history (order within this block may vary)
      googleCalendarUtils.deleteEvents("order-301")
      eventHistoryRepository.deleteByEventId("order-301")
      eventOccurrenceRepository.deleteByEventId("order-301")
      googleCalendarUtils.deleteEvents("order-302")
      eventHistoryRepository.deleteByEventId("order-302")
      eventOccurrenceRepository.deleteByEventId("order-302")
    }
  }

  @Test
  fun `invoke handles empty reminder list without errors`() = runTest {
    // Arrange
    val emptyList = emptyList<Reminder>()

    // Act
    useCase.invoke(emptyList)

    // Assert - No deactivation or deletion should occur
    coVerify(exactly = 0) { deactivateReminderUseCase(any()) }
    coVerify(exactly = 1) { reminderRepository.deleteAll(emptyList()) }
    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        ids = emptyList()
      )
    }
    coVerify(exactly = 0) { googleCalendarUtils.deleteEvents(any()) }
    coVerify(exactly = 0) { eventHistoryRepository.deleteByEventId(any()) }
    coVerify(exactly = 0) { eventOccurrenceRepository.deleteByEventId(any()) }
  }

  @Test
  fun `invoke handles single reminder correctly`() = runTest {
    // Arrange
    val singleReminder = listOf(
      Reminder(uuId = "single-401", summary = "Only one", syncState = SyncState.Synced)
    )

    // Act
    useCase.invoke(singleReminder)

    // Assert
    coVerify(exactly = 1) { deactivateReminderUseCase(singleReminder[0]) }
    coVerify(exactly = 1) { reminderRepository.deleteAll(listOf("single-401")) }
    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        ids = listOf("single-401")
      )
    }
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("single-401") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("single-401") }
    coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("single-401") }
  }

  @Test
  fun `invoke propagates exception when deactivation fails`() = runTest {
    // Arrange
    val reminders = listOf(
      Reminder(uuId = "fail-501", summary = "Will fail", syncState = SyncState.Synced),
      Reminder(uuId = "fail-502", summary = "Never reached", syncState = SyncState.Synced)
    )
    coEvery { deactivateReminderUseCase(reminders[0]) } throws IllegalStateException("Deactivation error")

    // Act & Assert
    try {
      useCase.invoke(reminders)
      assert(false) { "Exception expected but not thrown" }
    } catch (e: IllegalStateException) {
      // Verify first deactivation was attempted
      coVerify(exactly = 1) { deactivateReminderUseCase(reminders[0]) }
      // Second deactivation should not be reached
      coVerify(exactly = 0) { deactivateReminderUseCase(reminders[1]) }
      // No further operations should occur
      coVerify(exactly = 0) { reminderRepository.deleteAll(any()) }
      coVerify(exactly = 0) { scheduleBackgroundWorkUseCase(any(), any(), any()) }
    }
  }

  @Test
  fun `invoke propagates exception when repository delete fails and stops cleanup`() = runTest {
    // Arrange
    val reminders = listOf(
      Reminder(uuId = "repo-601", summary = "Repository fail", syncState = SyncState.Synced)
    )
    coEvery { reminderRepository.deleteAll(any()) } throws IllegalStateException("Repository error")

    // Act & Assert
    try {
      useCase.invoke(reminders)
      assert(false) { "Exception expected but not thrown" }
    } catch (e: IllegalStateException) {
      // Deactivation should complete
      coVerify(exactly = 1) { deactivateReminderUseCase(reminders[0]) }
      // Repository delete was attempted
      coVerify(exactly = 1) { reminderRepository.deleteAll(listOf("repo-601")) }
      // No further cleanup operations
      coVerify(exactly = 0) { scheduleBackgroundWorkUseCase(any(), any(), any()) }
      coVerify(exactly = 0) { googleCalendarUtils.deleteEvents(any()) }
      coVerify(exactly = 0) { eventHistoryRepository.deleteByEventId(any()) }
    }
  }

  @Test
  fun `invoke handles large batch of reminders efficiently`() = runTest {
    // Arrange - Create 100 reminders
    val largeReminderList = (1..100).map { index ->
      Reminder(
        uuId = "batch-${index.toString().padStart(3, '0')}",
        summary = "Reminder $index",
        syncState = SyncState.Synced
      )
    }
    val expectedIds = (1..100).map { "batch-${it.toString().padStart(3, '0')}" }

    // Act
    useCase.invoke(largeReminderList)

    // Assert
    // Each reminder should be deactivated once
    coVerify(exactly = 100) { deactivateReminderUseCase(any()) }
    // Repository deleteAll called once with all IDs
    coVerify(exactly = 1) { reminderRepository.deleteAll(expectedIds) }
    // Background work scheduled once with all IDs
    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        ids = expectedIds
      )
    }
    // Each reminder ID should have cleanup operations
    coVerify(exactly = 100) { googleCalendarUtils.deleteEvents(any()) }
    coVerify(exactly = 100) { eventHistoryRepository.deleteByEventId(any()) }
    coVerify(exactly = 100) { eventOccurrenceRepository.deleteByEventId(any()) }
  }

  @Test
  fun `invoke handles reminders with special characters in IDs`() = runTest {
    // Arrange
    val reminders = listOf(
      Reminder(uuId = "special-@#\$%", summary = "Special chars", syncState = SyncState.Synced),
      Reminder(uuId = "unicode-日本語", summary = "Unicode", syncState = SyncState.Synced),
      Reminder(uuId = "spaces in id", summary = "Has spaces", syncState = SyncState.Synced)
    )

    // Act
    useCase.invoke(reminders)

    // Assert - All special IDs should be handled correctly
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("special-@#\$%") }
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("unicode-日本語") }
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("spaces in id") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("special-@#\$%") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("unicode-日本語") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("spaces in id") }
  }
}
