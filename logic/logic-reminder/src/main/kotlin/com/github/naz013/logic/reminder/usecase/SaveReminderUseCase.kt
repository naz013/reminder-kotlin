package com.github.naz013.logic.reminder.usecase

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.ReminderWorkflowTrigger
import com.github.naz013.logic.reminder.ScheduleReminderUploadUseCase
import com.github.naz013.repository.ReminderV2Repository

class SaveReminderUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val scheduleReminderUploadUseCase: ScheduleReminderUploadUseCase,
  private val reminderWorkflowTrigger: ReminderWorkflowTrigger,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    val isNew = reminderV2Repository.getById(reminder.uuId) == null
    reminderV2Repository.save(reminder)
    appWidgetUpdater.updateScheduleWidget()
    scheduleReminderUploadUseCase(reminder.uuId)
    if (isNew) {
      reminderWorkflowTrigger.onReminderCreated(reminder.uuId)
    }
    Logger.i(TAG, "Saved reminder with id = ${reminder.uuId}")
  }

  companion object {
    private const val TAG = "SaveReminderUseCase"
  }
}
