package com.elementary.tasks.reminder.usecase

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.migration.toReminder
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository

class SaveReminderUseCase(
  private val reminderRepository: ReminderRepository,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val scheduleReminderUploadUseCase: ScheduleReminderUploadUseCase,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    // ReminderV2 has no jsonSchemaVersion field, so a bare .toReminder() would silently reset any
    // V1 row back to Reminder.Version.DEFAULT_VERSION on every save through this path (snooze/
    // activate/deactivate/etc). Force V3 instead, matching BuildReminderViewModel.saveReminder()'s
    // established convention: anything that has passed through the ReminderV2 shape is advanced to
    // the current schema, never silently downgraded.
    val v1Reminder = reminder.toReminder().apply { jsonSchemaVersion = Reminder.Version.V3 }
    reminderRepository.save(v1Reminder)
    appWidgetUpdater.updateScheduleWidget()
    scheduleReminderUploadUseCase(reminder.uuId)
    Logger.i(TAG, "Saved reminder with id = ${reminder.uuId}")
  }

  companion object {
    private const val TAG = "SaveReminderUseCase"
  }
}
