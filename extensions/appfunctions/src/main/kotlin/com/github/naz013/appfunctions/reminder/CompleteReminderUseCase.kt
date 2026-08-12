package com.github.naz013.appfunctions.reminder

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

class CompleteReminderUseCase(
  private val reminderV2Repository: ReminderV2Repository,
) {
  suspend operator fun invoke(id: String): ReminderV2? {
    val reminder = reminderV2Repository.getById(id) ?: return null
    val updated = reminder.copy(isActive = false)
    reminderV2Repository.save(updated)
    return updated
  }
}
