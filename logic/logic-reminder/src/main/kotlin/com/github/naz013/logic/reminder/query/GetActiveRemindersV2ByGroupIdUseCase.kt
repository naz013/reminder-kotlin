package com.github.naz013.logic.reminder.query

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

class GetActiveRemindersV2ByGroupIdUseCase(
  private val reminderV2Repository: ReminderV2Repository
) {

  suspend operator fun invoke(groupId: String): List<ReminderV2> {
    return reminderV2Repository.getByGroupId(groupId).filter { it.isActive && !it.isRemoved }
  }
}
