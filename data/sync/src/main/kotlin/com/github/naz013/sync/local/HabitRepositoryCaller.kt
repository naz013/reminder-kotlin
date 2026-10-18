package com.github.naz013.sync.local

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.HabitRepository

internal class HabitRepositoryCaller(
  private val habitRepository: HabitRepository
) : DataTypeRepositoryCaller<Habit> {

  override suspend fun getById(id: String): Habit? {
    return habitRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return habitRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    habitRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    require(item is Habit) { "Expected Habit type but got: ${item::class}" }
    habitRepository.save(item.copy(sync = item.sync.copy(syncState = SyncState.Synced)))
  }

  override suspend fun getAllIds(): List<String> {
    return habitRepository.getAllIds()
  }
}
