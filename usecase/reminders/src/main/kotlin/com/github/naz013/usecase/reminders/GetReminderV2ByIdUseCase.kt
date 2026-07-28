package com.github.naz013.usecase.reminders

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

class GetReminderV2ByIdUseCase(
  private val reminderV2Repository: ReminderV2Repository
) {

  suspend operator fun invoke(id: String): ReminderV2? {
    return reminderV2Repository.getById(id)
  }
}
