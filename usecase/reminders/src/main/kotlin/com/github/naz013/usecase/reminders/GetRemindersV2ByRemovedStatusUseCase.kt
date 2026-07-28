package com.github.naz013.usecase.reminders

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

class GetRemindersV2ByRemovedStatusUseCase(
  private val reminderV2Repository: ReminderV2Repository
) {

  suspend operator fun invoke(removed: Boolean): List<ReminderV2> {
    return reminderV2Repository.getByRemovedStatus(removed)
  }
}
