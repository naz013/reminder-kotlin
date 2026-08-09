package com.elementary.tasks.reminder.usecase

import com.github.naz013.logic.reminder.usecase.DeactivateReminderUseCase
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository

class MoveReminderToArchiveUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
) {
  suspend operator fun invoke(id: String) {
    val reminder =
      reminderV2Repository.getById(id) ?: run {
        Logger.w(TAG, "Reminder with id = $id not found")
        return
      }
    deactivateReminderUseCase(reminder.copy(isRemoved = true))
    Logger.i(TAG, "Moved reminder with id = $id to archive")
  }

  companion object {
    private const val TAG = "MoveReminderToArchiveUseCase"
  }
}
