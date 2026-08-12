package com.github.naz013.appfunctions.reminder

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

class DeleteReminderUseCase(
  private val reminderV2Repository: ReminderV2Repository,
) {
  suspend operator fun invoke(id: String): ReminderV2? {
    val reminder = reminderV2Repository.getById(id) ?: return null
    reminderV2Repository.delete(id)
    return reminder
  }
}
