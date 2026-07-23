package com.elementary.tasks.groups.usecase

import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository

class DeleteGroupUseCase(
  private val groupV2Repository: GroupV2Repository,
) {
  suspend operator fun invoke(groupId: String) {
    groupV2Repository.delete(groupId)
    Logger.i(TAG, "Deleted group with id = $groupId")
  }

  companion object {
    private const val TAG = "DeleteGroupUseCase"
  }
}
