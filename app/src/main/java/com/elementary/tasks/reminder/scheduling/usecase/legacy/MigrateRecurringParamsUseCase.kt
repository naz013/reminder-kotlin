package com.elementary.tasks.reminder.scheduling.usecase.legacy

import com.elementary.tasks.reminder.build.reminder.compose.ReminderDateTimeCleaner
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository

class MigrateRecurringParamsUseCase(
  private val reminderDateTimeCleaner: ReminderDateTimeCleaner,
  private val reminderRepository: ReminderRepository
) {

  suspend operator fun invoke() {
    val allReminders = reminderRepository.getAll()
    allReminders.forEach { reminder ->
      reminderDateTimeCleaner(reminder)
      reminderRepository.save(reminder)
    }
    Logger.i(TAG, "Migrated recurring params for ${allReminders.size} reminders.")
  }

  companion object {
    private const val TAG = "MigrateRecurringParamsUseCase"
  }
}
