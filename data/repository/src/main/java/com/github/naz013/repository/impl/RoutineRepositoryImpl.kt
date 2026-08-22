package com.github.naz013.repository.impl

import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.dao.RoutineDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoutineRepositoryImpl(
  private val dao: RoutineDao,
  private val tableChangeNotifier: TableChangeNotifier
) : RoutineRepository {

  private val table = Table.Routine

  override fun observeAll(): Flow<List<Routine>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

  override suspend fun getAll(): List<Routine> {
    Logger.d(TAG, "Get all routines")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getById(id: String): Routine? {
    Logger.d(TAG, "Get routine by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override fun observeById(id: String): Flow<Routine?> = dao.observeById(id).map { it?.toDomain() }

  override suspend fun save(routine: Routine) {
    Logger.d(TAG, "Save routine: ${routine.id}")
    dao.insert(routine.toEntity())
    tableChangeNotifier.notify(table)
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete routine: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all routines")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun setPinned(id: String, isPinned: Boolean) {
    Logger.d(TAG, "Set routine pinned: $id, isPinned=$isPinned")
    dao.setPinned(id, isPinned)
    tableChangeNotifier.notify(table)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    Logger.d(TAG, "Get routines by sync states: $states")
    return dao.getBySyncStates(states.map { it.name })
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Updating sync state for routine id: $id to state: $state")
    dao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAllIds(): List<String> {
    Logger.d(TAG, "Get all routine ids")
    return dao.getAllIds()
  }

  companion object {
    private const val TAG = "RoutineRepository"
  }
}
