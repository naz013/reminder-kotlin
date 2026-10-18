package com.github.naz013.repository

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
  fun observeAll(): Flow<List<Habit>>

  suspend fun getAll(): List<Habit>

  suspend fun getById(id: String): Habit?

  fun observeById(id: String): Flow<Habit?>

  suspend fun save(habit: Habit)

  suspend fun delete(id: String)

  suspend fun deleteAll()

  suspend fun setPinned(id: String, isPinned: Boolean)

  suspend fun getIdsByState(states: List<SyncState>): List<String>

  suspend fun updateSyncState(id: String, state: SyncState)

  suspend fun getAllIds(): List<String>
}
