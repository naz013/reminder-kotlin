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

class DeleteAllReminderUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val googleCalendarApi: GoogleCalendarApi,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val eventHistoryRepository: EventHistoryRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
) {
  suspend operator fun invoke(reminders: List<ReminderV2>) {
    reminders.forEach { deactivateReminderUseCase(it) }
    val ids = reminders.map { it.uuId }
    reminderV2Repository.deleteAll(ids)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.RemindersV2,
      ids = ids,
    )
    for (id in ids) {
      googleCalendarApi.deleteEvents(id)
      eventHistoryRepository.deleteByEventId(id)
      eventOccurrenceRepository.deleteByEventId(id)
      tagAssignmentRepository.detachAll(id, TaggedItemType.REMINDER)
    }
    Logger.i(TAG, "Deleted all reminders, count = ${ids.size}")
  }

  companion object {
    private const val TAG = "DeleteAllReminderUseCase"
  }
}
