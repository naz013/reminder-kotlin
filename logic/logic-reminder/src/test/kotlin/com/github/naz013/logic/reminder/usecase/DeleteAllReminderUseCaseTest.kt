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
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for DeleteAllReminderUseCase.
 *
 * Tests the bulk deletion behavior of multiple reminders including
 * deactivation, repository cleanup, calendar sync, and background work scheduling.
 */
class DeleteAllReminderUseCaseTest {
  private lateinit var reminderV2Repository: ReminderV2Repository
  private lateinit var googleCalendarUtils: GoogleCalendarApi
  private lateinit var scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
  private lateinit var deactivateReminderUseCase: DeactivateReminderUseCase
  private lateinit var eventOccurrenceRepository: EventOccurrenceRepository
  private lateinit var eventHistoryRepository: EventHistoryRepository
  private lateinit var tagAssignmentRepository: TagAssignmentRepository

  private lateinit var useCase: DeleteAllReminderUseCase

  private fun reminderV2(id: String, summary: String) =
    ReminderV2(uuId = id, summary = summary, schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

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
      DeleteAllReminderUseCase(
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
  fun `invoke deactivates all reminders and deletes them from repository`() =
    runTest {
      // Arrange
      val reminders =
        listOf(
          reminderV2("rem-001", "Meeting"),
          reminderV2("rem-002", "Call"),
          reminderV2("rem-003", "Task"),
        )

      // Act
      useCase.invoke(reminders)

      // Assert
      coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == reminders[0].uuId }) }
      coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == reminders[1].uuId }) }
      coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == reminders[2].uuId }) }
      coVerify(exactly = 1) { reminderV2Repository.deleteAll(listOf("rem-001", "rem-002", "rem-003")) }
    }

  @Test
  fun `invoke schedules background delete work with all reminder IDs`() =
    runTest {
      // Arrange
      val reminders =
        listOf(
          reminderV2("work-101", "Reminder 1"),
          reminderV2("work-102", "Reminder 2"),
        )

      // Act
      useCase.invoke(reminders)

      // Assert
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          ids = listOf("work-101", "work-102"),
        )
      }
    }

  @Test
  fun `invoke deletes calendar events and history for each reminder`() =
    runTest {
      // Arrange
      val reminders =
        listOf(
          reminderV2("cal-201", "Calendar reminder 1"),
          reminderV2("cal-202", "Calendar reminder 2"),
          reminderV2("cal-203", "Calendar reminder 3"),
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
      coVerify(exactly = 1) { tagAssignmentRepository.detachAll("cal-201", TaggedItemType.REMINDER) }
      coVerify(exactly = 1) { tagAssignmentRepository.detachAll("cal-202", TaggedItemType.REMINDER) }
      coVerify(exactly = 1) { tagAssignmentRepository.detachAll("cal-203", TaggedItemType.REMINDER) }
    }

  @Test
  fun `invoke calls operations in correct order`() =
    runTest {
      // Arrange
      val reminders =
        listOf(
          reminderV2("order-301", "Order test 1"),
          reminderV2("order-302", "Order test 2"),
        )

      // Act
      useCase.invoke(reminders)

      // Assert - Verify operations happen in the expected sequence
      coVerifyOrder {
        // First: deactivate all reminders
        deactivateReminderUseCase(match { it.uuId == reminders[0].uuId })
        deactivateReminderUseCase(match { it.uuId == reminders[1].uuId })

        // Second: delete from repository
        reminderV2Repository.deleteAll(listOf("order-301", "order-302"))

        // Third: schedule background work
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          ids = listOf("order-301", "order-302"),
        )

        // Fourth: cleanup calendar and history (order within this block may vary)
        googleCalendarUtils.deleteEvents("order-301")
        eventHistoryRepository.deleteByEventId("order-301")
        eventOccurrenceRepository.deleteByEventId("order-301")
        tagAssignmentRepository.detachAll("order-301", TaggedItemType.REMINDER)
        googleCalendarUtils.deleteEvents("order-302")
        eventHistoryRepository.deleteByEventId("order-302")
        eventOccurrenceRepository.deleteByEventId("order-302")
        tagAssignmentRepository.detachAll("order-302", TaggedItemType.REMINDER)
      }
    }

  @Test
  fun `invoke handles empty reminder list without errors`() =
    runTest {
      // Arrange
      val emptyList = emptyList<ReminderV2>()

      // Act
      useCase.invoke(emptyList)

      // Assert - No deactivation or deletion should occur
      coVerify(exactly = 0) { deactivateReminderUseCase(any()) }
      coVerify(exactly = 1) { reminderV2Repository.deleteAll(emptyList()) }
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          ids = emptyList(),
        )
      }
      coVerify(exactly = 0) { googleCalendarUtils.deleteEvents(any()) }
      coVerify(exactly = 0) { eventHistoryRepository.deleteByEventId(any()) }
      coVerify(exactly = 0) { eventOccurrenceRepository.deleteByEventId(any()) }
      coVerify(exactly = 0) { tagAssignmentRepository.detachAll(any(), any()) }
    }

  @Test
  fun `invoke handles single reminder correctly`() =
    runTest {
      // Arrange
      val singleReminder =
        listOf(
          reminderV2("single-401", "Only one"),
        )

      // Act
      useCase.invoke(singleReminder)

      // Assert
      coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == singleReminder[0].uuId }) }
      coVerify(exactly = 1) { reminderV2Repository.deleteAll(listOf("single-401")) }
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          ids = listOf("single-401"),
        )
      }
      coVerify(exactly = 1) { googleCalendarUtils.deleteEvents("single-401") }
      coVerify(exactly = 1) { eventHistoryRepository.deleteByEventId("single-401") }
      coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId("single-401") }
      coVerify(exactly = 1) { tagAssignmentRepository.detachAll("single-401", TaggedItemType.REMINDER) }
    }

  @Test
  fun `invoke propagates exception when deactivation fails`() =
    runTest {
      // Arrange
      val reminders =
        listOf(
          reminderV2("fail-501", "Will fail"),
          reminderV2("fail-502", "Never reached"),
        )
      coEvery { deactivateReminderUseCase(match { it.uuId == reminders[0].uuId }) } throws IllegalStateException("Deactivation error")

      // Act & Assert
      try {
        useCase.invoke(reminders)
        assert(false) { "Exception expected but not thrown" }
      } catch (e: IllegalStateException) {
        // Verify first deactivation was attempted
        coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == reminders[0].uuId }) }
        // Second deactivation should not be reached
        coVerify(exactly = 0) { deactivateReminderUseCase(match { it.uuId == reminders[1].uuId }) }
        // No further operations should occur
        coVerify(exactly = 0) { reminderV2Repository.deleteAll(any()) }
        coVerify(exactly = 0) { scheduleBackgroundWorkUseCase(any(), any(), any()) }
      }
    }

  @Test
  fun `invoke propagates exception when repository delete fails and stops cleanup`() =
    runTest {
      // Arrange
      val reminders =
        listOf(
          reminderV2("repo-601", "Repository fail"),
        )
      coEvery { reminderV2Repository.deleteAll(any()) } throws IllegalStateException("Repository error")

      // Act & Assert
      try {
        useCase.invoke(reminders)
        assert(false) { "Exception expected but not thrown" }
      } catch (e: IllegalStateException) {
        // Deactivation should complete
        coVerify(exactly = 1) { deactivateReminderUseCase(match { it.uuId == reminders[0].uuId }) }
        // Repository delete was attempted
        coVerify(exactly = 1) { reminderV2Repository.deleteAll(listOf("repo-601")) }
        // No further cleanup operations
        coVerify(exactly = 0) { scheduleBackgroundWorkUseCase(any(), any(), any()) }
        coVerify(exactly = 0) { googleCalendarUtils.deleteEvents(any()) }
        coVerify(exactly = 0) { eventHistoryRepository.deleteByEventId(any()) }
      }
    }

  @Test
  fun `invoke handles large batch of reminders efficiently`() =
    runTest {
      // Arrange - Create 100 reminders
      val largeReminderList =
        (1..100).map { index ->
          reminderV2("batch-${index.toString().padStart(3, '0')}", "Reminder $index")
        }
      val expectedIds = (1..100).map { "batch-${it.toString().padStart(3, '0')}" }

      // Act
      useCase.invoke(largeReminderList)

      // Assert
      // Each reminder should be deactivated once
      coVerify(exactly = 100) { deactivateReminderUseCase(any()) }
      // Repository deleteAll called once with all IDs
      coVerify(exactly = 1) { reminderV2Repository.deleteAll(expectedIds) }
      // Background work scheduled once with all IDs
      coVerify(exactly = 1) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.Delete,
          dataType = DataType.RemindersV2,
          ids = expectedIds,
        )
      }
      // Each reminder ID should have cleanup operations
      coVerify(exactly = 100) { googleCalendarUtils.deleteEvents(any()) }
      coVerify(exactly = 100) { eventHistoryRepository.deleteByEventId(any()) }
      coVerify(exactly = 100) { eventOccurrenceRepository.deleteByEventId(any()) }
    }

  @Test
  fun `invoke handles reminders with special characters in IDs`() =
    runTest {
      // Arrange
      val reminders =
        listOf(
          reminderV2("special-@#\$%", "Special chars"),
          reminderV2("unicode-日本語", "Unicode"),
          reminderV2("spaces in id", "Has spaces"),
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
