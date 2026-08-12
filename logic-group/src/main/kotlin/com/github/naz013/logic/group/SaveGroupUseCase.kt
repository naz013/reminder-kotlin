package com.github.naz013.logic.group

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository

class SaveGroupUseCase(
  private val groupV2Repository: GroupV2Repository,
  private val makeGroupDefaultUseCase: MakeGroupDefaultUseCase,
) {
  suspend operator fun invoke(group: GroupV2) {
    groupV2Repository.save(group.copy(version = group.version + 1, syncState = SyncState.WaitingForUpload))
    if (group.isDefault) {
      makeGroupDefaultUseCase(group.uuId)
    }
    Logger.i(TAG, "Saved group: ${group.uuId}")
  }

  companion object {
    private const val TAG = "SaveGroupUseCase"
  }
}
