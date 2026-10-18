package com.github.naz013.repository

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

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

  fun observeCheckIns(habitId: String, from: LocalDate, to: LocalDate): Flow<List<HabitCheckIn>>

  suspend fun getCheckIns(habitId: String, from: LocalDate, to: LocalDate): List<HabitCheckIn>

  suspend fun getCheckIn(habitId: String, date: LocalDate): HabitCheckIn?

  suspend fun saveCheckIn(checkIn: HabitCheckIn)

  suspend fun deleteCheckIn(habitId: String, date: LocalDate)

  suspend fun deleteCheckIns(habitId: String)
}
