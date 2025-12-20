package com.elementary.tasks.reminder.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.scheduling.usecase.DeactivateReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.sync.DataType

class DeleteReminderUseCase(
  private val reminderRepository: ReminderRepository,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val eventHistoryRepository: EventHistoryRepository,
) {

  suspend operator fun invoke(reminder: Reminder) {
    deactivateReminderUseCase(reminder)
    reminderRepository.delete(reminder.uuId)
    googleCalendarUtils.deleteEvents(reminder.uuId)
    eventHistoryRepository.deleteByEventId(reminder.uuId)
    eventOccurrenceRepository.deleteByEventId(reminder.uuId)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Reminders,
      id = reminder.uuId
    )
    Logger.i(TAG, "Deleted reminder with id = ${reminder.uuId}")
  }

  companion object {
    private const val TAG = "DeleteReminderUseCase"
  }
}
