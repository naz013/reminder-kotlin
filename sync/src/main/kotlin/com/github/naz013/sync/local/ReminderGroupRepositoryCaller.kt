package com.github.naz013.sync.local

import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderGroupRepository

internal class ReminderGroupRepositoryCaller(
  private val reminderGroupRepository: ReminderGroupRepository
) : DataTypeRepositoryCaller<ReminderGroup> {

  override suspend fun getById(id: String): ReminderGroup? {
    return reminderGroupRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return reminderGroupRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    reminderGroupRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: ReminderGroup) {
    reminderGroupRepository.save(item)
  }
}
