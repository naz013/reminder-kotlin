package com.elementary.tasks.groups

import com.elementary.tasks.R
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderGroupRepository
import java.util.Random

class GroupsUtil(
  private val textProvider: TextProvider,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val groupV2Repository: GroupV2Repository,
  private val dateTimeManager: DateTimeManager,
) {
  private val random = Random()

  suspend fun initDefaultIfEmpty() {
    if (reminderGroupRepository.getAll().isEmpty()) {
      initDefault()
    }
    if (groupV2Repository.getAll().isEmpty()) {
      initDefaultV2()
    }
  }

  private suspend fun initDefault(): String {
    val def =
      ReminderGroup(
        groupTitle = textProvider.getText(R.string.general),
        groupColor = random.nextInt(16),
        groupDateTime = dateTimeManager.getNowGmtDateTime(),
        isDefaultGroup = true,
        groupUuId = getGroupId(1),
        syncState = SyncState.WaitingForUpload,
      )
    runCatching {
      reminderGroupRepository.save(def)
      reminderGroupRepository.save(
        ReminderGroup(
          groupTitle = textProvider.getText(R.string.work),
          groupColor = random.nextInt(16),
          groupDateTime = dateTimeManager.getNowGmtDateTime(),
          isDefaultGroup = false,
          groupUuId = getGroupId(2),
          syncState = SyncState.WaitingForUpload,
        ),
      )
      reminderGroupRepository.save(
        ReminderGroup(
          groupTitle = textProvider.getText(R.string.personal),
          groupColor = random.nextInt(16),
          groupDateTime = dateTimeManager.getNowGmtDateTime(),
          isDefaultGroup = false,
          groupUuId = getGroupId(3),
          syncState = SyncState.WaitingForUpload,
        ),
      )
    }
    return def.groupUuId
  }

  /** Seeds the same default groups (matching ids) into GroupV2, so a fresh install's Group
   * screens have something to show even though they no longer read [ReminderGroup]. */
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
