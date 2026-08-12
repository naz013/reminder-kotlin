package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.files.DataType
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository

class DeleteReminderUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val googleCalendarApi: GoogleCalendarApi,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val eventHistoryRepository: EventHistoryRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    deactivateReminderUseCase(reminder)
    reminderV2Repository.delete(reminder.uuId)
    googleCalendarApi.deleteEvents(reminder.uuId)
    eventHistoryRepository.deleteByEventId(reminder.uuId)
    eventOccurrenceRepository.deleteByEventId(reminder.uuId)
    tagAssignmentRepository.detachAll(reminder.uuId, TaggedItemType.REMINDER)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.RemindersV2,
      id = reminder.uuId,
    )
    Logger.i(TAG, "Deleted reminder with id = ${reminder.uuId}")
  }

  companion object {
    private const val TAG = "DeleteReminderUseCase"
  }
}
