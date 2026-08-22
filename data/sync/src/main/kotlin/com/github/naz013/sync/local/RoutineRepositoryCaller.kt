package com.github.naz013.sync.local

import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.RoutineRepository

internal class RoutineRepositoryCaller(
  private val routineRepository: RoutineRepository
) : DataTypeRepositoryCaller<Routine> {

  override suspend fun getById(id: String): Routine? {
    return routineRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return routineRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    routineRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is Routine) {
      throw IllegalArgumentException("Expected Routine type but got: ${item::class}")
    }
    routineRepository.save(item.copy(sync = item.sync.copy(syncState = SyncState.Synced)))
  }

  override suspend fun getAllIds(): List<String> {
    return routineRepository.getAllIds()
  }
}
