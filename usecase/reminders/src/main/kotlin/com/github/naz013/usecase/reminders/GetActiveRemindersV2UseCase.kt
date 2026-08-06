package com.github.naz013.usecase.reminders

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

class GetActiveRemindersV2UseCase(
  private val reminderV2Repository: ReminderV2Repository
) {

  suspend operator fun invoke(): List<ReminderV2> {
    return reminderV2Repository.getAll(active = true, removed = false)
  }
}
