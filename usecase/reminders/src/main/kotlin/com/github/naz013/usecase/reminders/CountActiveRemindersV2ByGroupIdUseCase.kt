package com.github.naz013.usecase.reminders

import com.github.naz013.repository.ReminderV2Repository

class CountActiveRemindersV2ByGroupIdUseCase(
  private val reminderV2Repository: ReminderV2Repository
) {

  suspend operator fun invoke(groupId: String): Int {
    return reminderV2Repository.countActiveByGroupId(groupId)
  }
}
