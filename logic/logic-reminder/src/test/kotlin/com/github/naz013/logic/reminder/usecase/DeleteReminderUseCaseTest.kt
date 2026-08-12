package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.files.DataType
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for DeleteReminderUseCase.
 *
 * Covers core behavior, call order, error propagation, and edge cases.
 */
class DeleteReminderUseCaseTest {
  private lateinit var reminderV2Repository: ReminderV2Repository
  private lateinit var googleCalendarUtils: GoogleCalendarApi
  private lateinit var scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
  private lateinit var deactivateReminderUseCase: DeactivateReminderUseCase
  private lateinit var eventOccurrenceRepository: EventOccurrenceRepository
  private lateinit var eventHistoryRepository: EventHistoryRepository
  private lateinit var tagAssignmentRepository: TagAssignmentRepository

  private lateinit var useCase: DeleteReminderUseCase

  @Before
  fun setUp() {
    reminderV2Repository = mockk(relaxed = true)
    googleCalendarUtils = mockk(relaxed = true)
    scheduleBackgroundWorkUseCase = mockk(relaxed = true)
    deactivateReminderUseCase = mockk(relaxed = true)
    eventOccurrenceRepository = mockk(relaxed = true)
    eventHistoryRepository = mockk(relaxed = true)
    tagAssignmentRepository = mockk(relaxed = true)

    useCase =
      DeleteReminderUseCase(
        reminderV2Repository = reminderV2Repository,
        googleCalendarApi = googleCalendarUtils,
        scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
        deactivateReminderUseCase = deactivateReminderUseCase,
        eventOccurrenceRepository = eventOccurrenceRepository,
        eventHistoryRepository = eventHistoryRepository,
        tagAssignmentRepository = tagAssignmentRepository,
      )
  }

  @Test
  fun `invoke performs all deletion steps and schedules background work`() =
    runTest {
      // Arrange
      val reminder =
        ReminderV2(uuId = "id-123", summary = "Test", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

      // Act
      useCase.invoke(reminder)

      // Assert
      coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == reminder.uuId }) }
      coVerify(exactly = 1) { reminderV2Repository.delete("id-123") }
      coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("id-123") }
      coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("id-123") }
      coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("id-123") }
      coVerify(exactly = 1) { tagAssignmentRepository.detachAll("id-123", TaggedItemType.REMINDER) }
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          id = "id-123",
        )
      }
    }

  @Test
  fun `invoke calls methods in correct order`() =
    runTest {
      // Arrange
      val reminder = ReminderV2(
        uuId = "id-456",
        summary = "Order Test",
        schedule = ReminderSchedule(startDateTime = LocalDateTime.now())
      )

      // Act
      useCase.invoke(reminder)

      // Assert - Verify the exact order of side-effect calls
      coVerifyOrder {
        deactivateReminderUseCase(match { it.uuId == reminder.uuId })
        reminderV2Repository.delete("id-456")
        googleCalendarUtils.deleteEvents("id-456")
        eventHistoryRepository.deleteByEventId("id-456")
        eventOccurrenceRepository.deleteByEventId("id-456")
        tagAssignmentRepository.detachAll("id-456", TaggedItemType.REMINDER)
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          id = "id-456",
        )
      }
    }

  @Test
  fun `invoke schedules delete work with correct parameters`() =
    runTest {
      // Arrange
      val reminder = ReminderV2(
        uuId = "work-789",
        summary = "Work Params",
        schedule = ReminderSchedule(startDateTime = LocalDateTime.now())
      )

      // Act
      useCase.invoke(reminder)

      // Assert
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          id = "work-789",
        )
      }
    }

  @Test
  fun `invoke propagates exception when repository delete fails and stops subsequent calls`() =
    runTest {
      // Arrange
      val reminder = ReminderV2(
        uuId = "fail-001",
        summary = "Failure",
        schedule = ReminderSchedule(startDateTime = LocalDateTime.now())
      )
      coJustRun { deactivateReminderUseCase(match { it.uuId == reminder.uuId }) }
      coEvery { reminderV2Repository.delete("fail-001") } throws IllegalStateException("DB error")

      // Act
      try {
        useCase.invoke(reminder)
        assert(false) { "Exception expected but not thrown" }
      } catch (e: IllegalStateException) {
        // Assert
        // Deactivate was called and delete threw an exception
        coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == reminder.uuId }) }
        coVerify(exactly = 1) { reminderV2Repository.delete("fail-001") }
        // No further calls were made
        coVerify(exactly = 0) { googleCalendarUtils.deleteEvents(any()) }
        coVerify(exactly = 0) { eventHistoryRepository.deleteByEventId(any()) }
        coVerify(exactly = 0) { eventOccurrenceRepository.deleteByEventId(any()) }
        coVerify(exactly = 0) { tagAssignmentRepository.detachAll(any(), any()) }
        coVerify(exactly = 0) { scheduleBackgroundWorkUseCase(any(), any(), any()) }
      }
    }

  @Test
  fun `invoke works with empty reminder id and still calls collaborators`() =
    runTest {
      // Arrange
      val reminder =
        ReminderV2(uuId = "", summary = "Empty ID", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

      // Act
      useCase.invoke(reminder)

      // Assert - All calls still happen with empty id string
      coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == reminder.uuId }) }
      coVerify(exactly = 1) { reminderV2Repository.delete("") }
      coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("") }
      coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("") }
      coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("") }
      coVerify(exactly = 1) { tagAssignmentRepository.detachAll("", TaggedItemType.REMINDER) }
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          id = "",
        )
      }
    }

  @Test
  fun `invoke deactivates reminder before deletion`() =
    runTest {
      // Arrange
      val reminder = ReminderV2(
        uuId = "seq-002",
        summary = "Sequence Test",
        schedule = ReminderSchedule(startDateTime = LocalDateTime.now())
      )

      // Act
      useCase.invoke(reminder)

      // Assert a partial order: deactivate called before repository delete
      coVerifyOrder {
        deactivateReminderUseCase(match { it.uuId == reminder.uuId })
        reminderV2Repository.delete("seq-002")
      }
    }
}
