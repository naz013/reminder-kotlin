package com.github.naz013.sync.local

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderV2Repository

internal class ReminderV2RepositoryCaller(
  private val reminderV2Repository: ReminderV2Repository
) : DataTypeRepositoryCaller<ReminderV2> {

  override suspend fun getById(id: String): ReminderV2? {
    return reminderV2Repository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return reminderV2Repository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    reminderV2Repository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is ReminderV2) {
      throw IllegalArgumentException("Expected ReminderV2 type but got: ${item::class}")
    }
    reminderV2Repository.save(item.copy(sync = item.sync.copy(syncState = SyncState.Synced)))
  }

  override suspend fun getAllIds(): List<String> {
    return reminderV2Repository.getAllIds()
  }
}
