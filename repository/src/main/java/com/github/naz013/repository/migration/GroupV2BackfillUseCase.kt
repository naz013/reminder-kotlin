package com.github.naz013.repository.migration

import com.github.naz013.domain.reminder.migration.toGroupV2
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository

/**
 * One-time migration step: populates the GroupV2 table from existing V1 ReminderGroup rows,
 * preserving group ids so ReminderV2.groupId references keep resolving correctly. Intended to be
 * invoked once at app startup, same as [ReminderV2BackfillUseCase].
 */
class GroupV2BackfillUseCase internal constructor(
  private val reminderGroupRepository: ReminderGroupRepository,
  private val groupV2Repository: GroupV2Repository
) {

  suspend operator fun invoke() {
    val existingIds = groupV2Repository.getAllIds().toSet()
    val groups = reminderGroupRepository.getAll()
    Logger.d(TAG, "Backfilling ${groups.size} groups into GroupV2")
    groups
      .filterNot { existingIds.contains(it.groupUuId) }
      .forEach { group ->
        runCatching {
          groupV2Repository.save(group.toGroupV2())
        }.onFailure {
          Logger.e(TAG, "Failed to backfill group ${group.groupUuId}", it)
        }
      }
  }

  companion object {
    private const val TAG = "GroupV2Backfill"
  }
}
