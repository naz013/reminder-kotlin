package com.elementary.tasks.reminder.usecase

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.scheduling.usecase.DeactivateReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.sync.DataType
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteReminderUseCase.
 *
 * Covers core behavior, call order, error propagation, and edge cases.
 */
class DeleteReminderUseCaseTest : BaseTest() {

  private lateinit var reminderRepository: ReminderRepository
  private lateinit var googleCalendarUtils: GoogleCalendarUtils
  private lateinit var scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
  private lateinit var deactivateReminderUseCase: DeactivateReminderUseCase
  private lateinit var eventOccurrenceRepository: EventOccurrenceRepository
  private lateinit var eventHistoryRepository: EventHistoryRepository

  private lateinit var useCase: DeleteReminderUseCase

  @Before
  override fun setUp() {
    super.setUp()
    reminderRepository = mockk(relaxed = true)
    googleCalendarUtils = mockk(relaxed = true)
    scheduleBackgroundWorkUseCase = mockk(relaxed = true)
    deactivateReminderUseCase = mockk(relaxed = true)
    eventOccurrenceRepository = mockk(relaxed = true)
    eventHistoryRepository = mockk(relaxed = true)

    useCase = DeleteReminderUseCase(
      reminderRepository = reminderRepository,
      googleCalendarUtils = googleCalendarUtils,
      scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
      deactivateReminderUseCase = deactivateReminderUseCase,
      eventOccurrenceRepository = eventOccurrenceRepository,
      eventHistoryRepository = eventHistoryRepository
    )
  }

  @Test
  fun `invoke performs all deletion steps and schedules background work`() = runTest {
    // Arrange
    val reminder = Reminder(uuId = "id-123", summary = "Test")

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 1) { deactivateReminderUseCase(reminder) }
    coVerify(exactly = 1) { reminderRepository.delete("id-123") }
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("id-123") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("id-123") }
    coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("id-123") }
    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        id = "id-123"
      )
    }
  }

  @Test
  fun `invoke calls methods in correct order`() = runTest {
    // Arrange
    val reminder = Reminder(uuId = "id-456", summary = "Order Test")

    // Act
    useCase.invoke(reminder)

    // Assert - Verify the exact order of side-effect calls
    coVerifyOrder {
      deactivateReminderUseCase(reminder)
      reminderRepository.delete("id-456")
      googleCalendarUtils.deleteEvents("id-456")
      eventHistoryRepository.deleteByEventId("id-456")
      eventOccurrenceRepository.deleteByEventId("id-456")
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        id = "id-456"
      )
    }
  }

  @Test
  fun `invoke schedules delete work with correct parameters`() = runTest {
    // Arrange
    val reminder = Reminder(uuId = "work-789", summary = "Work Params")

    // Act
    useCase.invoke(reminder)

    // Assert
    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        id = "work-789"
      )
    }
  }

  @Test
  fun `invoke propagates exception when repository delete fails and stops subsequent calls`() = runTest {
    // Arrange
    val reminder = Reminder(uuId = "fail-001", summary = "Failure")
    coJustRun { deactivateReminderUseCase(reminder) }
    coEvery { reminderRepository.delete("fail-001") } throws IllegalStateException("DB error")

    // Act
    try {
      useCase.invoke(reminder)
      assert(false) { "Exception expected but not thrown" }
    } catch (e: IllegalStateException) {
      // Assert
      // Deactivate was called and delete threw an exception
      coVerify(exactly = 1) { deactivateReminderUseCase(reminder) }
      coVerify(exactly = 1) { reminderRepository.delete("fail-001") }
      // No further calls were made
      coVerify(exactly = 0) { googleCalendarUtils.deleteEvents(any()) }
      coVerify(exactly = 0) { eventHistoryRepository.deleteByEventId(any()) }
      coVerify(exactly = 0) { eventOccurrenceRepository.deleteByEventId(any()) }
      coVerify(exactly = 0) { scheduleBackgroundWorkUseCase(any(), any(), any()) }
    }
  }

  @Test
  fun `invoke works with empty reminder id and still calls collaborators`() = runTest {
    // Arrange
    val reminder = Reminder(uuId = "", summary = "Empty ID")

    // Act
    useCase.invoke(reminder)

    // Assert - All calls still happen with empty id string
    coVerify(exactly = 1) { deactivateReminderUseCase(reminder) }
    coVerify(exactly = 1) { reminderRepository.delete("") }
    coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("") }
    coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("") }
    coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("") }
    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Reminders,
        id = ""
      )
    }
  }

  @Test
  fun `invoke deactivates reminder before deletion`() = runTest {
    // Arrange
    val reminder = Reminder(uuId = "seq-002", summary = "Sequence Test")

    // Act
    useCase.invoke(reminder)

    // Assert a partial order: deactivate called before repository delete
    coVerifyOrder {
      deactivateReminderUseCase(reminder)
      reminderRepository.delete("seq-002")
    }
  }
}
