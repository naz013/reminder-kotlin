package com.elementary.tasks.groups.usecase

import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository

class DeleteGroupUseCase(
  private val groupV2Repository: GroupV2Repository,
  private val reminderV2Repository: ReminderV2Repository,
) {
  suspend operator fun invoke(groupId: String) {
    reminderV2Repository.clearGroupId(groupId)
    groupV2Repository.delete(groupId)
    Logger.i(TAG, "Deleted group with id = $groupId")
  }

  companion object {
    private const val TAG = "DeleteGroupUseCase"
  }
}
