package com.github.naz013.ui.group

import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.ui.common.R
import java.util.Random

class GroupsUtil(
  private val textProvider: TextProvider,
  private val groupV2Repository: GroupV2Repository,
  private val dateTimeManager: DateTimeManager,
) {
  private val random = Random()

  suspend fun initDefaultIfEmpty() {
    if (groupV2Repository.getAll().isEmpty()) {
      initDefaultV2()
    }
  }

  private suspend fun initDefaultV2() {
    runCatching {
      groupV2Repository.save(
        GroupV2(
          uuId = getGroupId(1),
          title = textProvider.getText(R.string.general),
          color = random.nextInt(16),
          isDefault = true,
          createdAt = dateTimeManager.getCurrentDateTime(),
          syncState = SyncState.WaitingForUpload,
        ),
      )
      groupV2Repository.save(
        GroupV2(
          uuId = getGroupId(2),
          title = textProvider.getText(R.string.work),
          color = random.nextInt(16),
          isDefault = false,
          createdAt = dateTimeManager.getCurrentDateTime(),
          syncState = SyncState.WaitingForUpload,
        ),
      )
      groupV2Repository.save(
        GroupV2(
          uuId = getGroupId(3),
          title = textProvider.getText(R.string.personal),
          color = random.nextInt(16),
          isDefault = false,
          createdAt = dateTimeManager.getCurrentDateTime(),
          syncState = SyncState.WaitingForUpload,
        ),
      )
    }
  }

  private fun getGroupId(index: Int): String = "default_group_$index"
}
