package com.github.naz013.repository.impl

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.HabitRepository
import com.github.naz013.repository.dao.HabitDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class HabitRepositoryImpl(
  private val habitDao: HabitDao,
  private val tableChangeNotifier: TableChangeNotifier
) : HabitRepository {

  override fun observeAll(): Flow<List<Habit>> = habitDao.observeAll().map { list -> list.map { it.toDomain() } }

  override suspend fun getAll(): List<Habit> {
    Logger.d(TAG, "Get all habits")
    return habitDao.getAll().map { it.toDomain() }
  }

  override suspend fun getById(id: String): Habit? {
    Logger.d(TAG, "Get habit by id: $id")
    return habitDao.getById(id)?.toDomain()
  }

  override fun observeById(id: String): Flow<Habit?> = habitDao.observeById(id).map { it?.toDomain() }

  override suspend fun save(habit: Habit) {
    Logger.d(TAG, "Save habit: ${habit.id}")
    habitDao.insert(habit.toEntity())
    tableChangeNotifier.notify(Table.Habit)
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete habit: $id")
    habitDao.deleteWithCheckIns(id)
    tableChangeNotifier.notify(Table.HabitCheckIn)
    tableChangeNotifier.notify(Table.Habit)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all habits")
    habitDao.deleteAllWithCheckIns()
    tableChangeNotifier.notify(Table.HabitCheckIn)
    tableChangeNotifier.notify(Table.Habit)
  }

  override suspend fun setPinned(id: String, isPinned: Boolean) {
    Logger.d(TAG, "Set habit pinned: $id, isPinned=$isPinned")
    habitDao.setPinned(id, isPinned)
    tableChangeNotifier.notify(Table.Habit)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    Logger.d(TAG, "Get habits by sync states: $states")
    return habitDao.getBySyncStates(states.map { it.name })
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Updating sync state for habit id: $id to state: $state")
    habitDao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(Table.Habit)
  }

  override suspend fun getAllIds(): List<String> {
    Logger.d(TAG, "Get all habit ids")
    return habitDao.getAllIds()
  }

  companion object {
    private const val TAG = "HabitRepository"
  }
}
