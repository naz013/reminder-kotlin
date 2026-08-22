package com.github.naz013.repository

import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
  fun observeAll(): Flow<List<Routine>>

  suspend fun getAll(): List<Routine>

  suspend fun getById(id: String): Routine?

  fun observeById(id: String): Flow<Routine?>

  suspend fun save(routine: Routine)

  suspend fun delete(id: String)

  suspend fun deleteAll()

  suspend fun setPinned(id: String, isPinned: Boolean)

  suspend fun getIdsByState(states: List<SyncState>): List<String>

  suspend fun updateSyncState(id: String, state: SyncState)

  suspend fun getAllIds(): List<String>
}
