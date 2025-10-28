package com.elementary.tasks.reminder.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.logging.Logger
import com.github.naz013.sync.DataType

class ScheduleReminderUploadUseCase(
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {

  suspend operator fun invoke(id: String) {
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Reminders,
      id = id
    )
    Logger.i(TAG, "Scheduled upload for reminder with id = $id")
  }

  companion object {
    private const val TAG = "ScheduleReminderUploadUseCase"
  }
}
