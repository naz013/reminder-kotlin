package com.elementary.tasks.reminder.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.sync.DataType

class DeleteAllReminderUseCase(
  private val reminderRepository: ReminderRepository,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  suspend operator fun invoke(ids: List<String>) {
    reminderRepository.deleteAll(ids)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Reminders,
      ids = ids
    )
    for (id in ids) {
      googleCalendarUtils.deleteEvents(id)
    }
    Logger.i(TAG, "Deleted all reminders, count = ${ids.size}")
  }

  companion object {
    private const val TAG = "DeleteAllReminderUseCase"
  }
}
