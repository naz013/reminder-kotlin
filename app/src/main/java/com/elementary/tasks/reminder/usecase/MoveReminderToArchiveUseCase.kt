package com.elementary.tasks.reminder.usecase

import com.elementary.tasks.reminder.scheduling.usecase.DeactivateReminderUseCase
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository

class MoveReminderToArchiveUseCase(
  private val reminderRepository: ReminderRepository,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
) {

  suspend operator fun invoke(id: String) {
    val reminder = reminderRepository.getById(id) ?: run {
      Logger.w(TAG, "Reminder with id = $id not found")
      return
    }
    reminder.isRemoved = true
    deactivateReminderUseCase(reminder)
    Logger.i(TAG, "Moved reminder with id = $id to archive")
  }

  companion object {
    private const val TAG = "MoveReminderToArchiveUseCase"
  }
}
