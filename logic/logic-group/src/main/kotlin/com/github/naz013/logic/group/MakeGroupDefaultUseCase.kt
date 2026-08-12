package com.github.naz013.logic.group

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository

class MakeGroupDefaultUseCase(
  private val groupV2Repository: GroupV2Repository,
) {
  suspend operator fun invoke(id: String) {
    groupV2Repository.getById(id) ?: run {
      Logger.e(TAG, "Group not found: $id")
      return
    }
    groupV2Repository
      .getAll()
      .filter { it.isDefault }
      .filterNot { it.uuId == id }
      .forEach {
        groupV2Repository.setDefaultGroup(it.uuId, false)
        groupV2Repository.updateSyncState(it.uuId, SyncState.WaitingForUpload)
      }

    groupV2Repository.setDefaultGroup(id, true)
    Logger.i(TAG, "Group set as default: $id")
  }

  companion object {
    private const val TAG = "MakeGroupDefaultUseCase"
  }
}
