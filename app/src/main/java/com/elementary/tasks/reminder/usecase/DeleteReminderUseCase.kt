package com.elementary.tasks.reminder.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.sync.DataType

class DeleteReminderUseCase(
  private val eventControlFactory: EventControlFactory,
  private val reminderRepository: ReminderRepository,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  suspend operator fun invoke(reminder: Reminder) {
    eventControlFactory.getController(reminder).disable()
    reminderRepository.delete(reminder.uuId)
    googleCalendarUtils.deleteEvents(reminder.uuId)
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
