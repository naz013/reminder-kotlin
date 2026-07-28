package com.github.naz013.sync.local

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.GroupV2Repository

internal class GroupV2RepositoryCaller(
  private val groupV2Repository: GroupV2Repository
) : DataTypeRepositoryCaller<GroupV2> {

  override suspend fun getById(id: String): GroupV2? {
    return groupV2Repository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return groupV2Repository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    groupV2Repository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is GroupV2) {
      throw IllegalArgumentException("Expected GroupV2 type but got: ${item::class}")
    }
    groupV2Repository.save(item.copy(syncState = SyncState.Synced))
  }

  override suspend fun getAllIds(): List<String> {
    return groupV2Repository.getAllIds()
  }
}
