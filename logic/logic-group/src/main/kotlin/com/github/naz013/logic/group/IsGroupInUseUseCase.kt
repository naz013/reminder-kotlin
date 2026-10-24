package com.github.naz013.logic.group

import com.github.naz013.repository.ReminderV2Repository

class IsGroupInUseUseCase(
  private val reminderV2Repository: ReminderV2Repository,
) {

  suspend operator fun invoke(groupId: String): Boolean {
    return reminderV2Repository.countActiveByGroupId(groupId) > 0
  }

  suspend fun anyInUse(groupIds: Collection<String>): Boolean {
    return groupIds.any { invoke(it) }
  }
}
