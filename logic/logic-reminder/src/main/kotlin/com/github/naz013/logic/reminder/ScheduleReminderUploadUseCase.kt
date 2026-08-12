package com.github.naz013.logic.reminder

import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType

class ScheduleReminderUploadUseCase(
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  suspend operator fun invoke(id: String) {
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.RemindersV2,
      id = id,
    )
    Logger.i(TAG, "Scheduled upload for reminder with id = $id")
  }

  companion object {
    private const val TAG = "ScheduleReminderUploadUseCase"
  }
}
