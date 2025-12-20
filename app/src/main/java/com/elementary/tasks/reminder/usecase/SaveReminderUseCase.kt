package com.elementary.tasks.reminder.usecase

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository

class SaveReminderUseCase(
  private val reminderRepository: ReminderRepository,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val scheduleReminderUploadUseCase: ScheduleReminderUploadUseCase
) {

  suspend operator fun invoke(reminder: Reminder) {
    reminderRepository.save(reminder)
    appWidgetUpdater.updateScheduleWidget()
    scheduleReminderUploadUseCase(reminder.uuId)
    Logger.i(TAG, "Saved reminder with id = ${reminder.uuId}")
  }

  companion object {
    private const val TAG = "SaveReminderUseCase"
  }
}
