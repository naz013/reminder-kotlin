package com.github.naz013.sync.local

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderRepository

internal class ReminderRepositoryCaller(
  private val reminderRepository: ReminderRepository
) : DataTypeRepositoryCaller<Reminder> {

  override suspend fun getById(id: String): Reminder? {
    return reminderRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return reminderRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    reminderRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is Reminder) {
      throw IllegalArgumentException("Expected Reminder type but got: ${item::class}")
    }
    reminderRepository.save(item.copy(syncState = SyncState.Synced))
  }

  override suspend fun getAllIds(): List<String> {
    return reminderRepository.getAllIds()
  }
}
