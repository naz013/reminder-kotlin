package com.github.naz013.repository.impl

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.HabitRepository
import com.github.naz013.repository.dao.HabitCheckInDao
import com.github.naz013.repository.dao.HabitDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDate

internal class HabitRepositoryImpl(
  private val habitDao: HabitDao,
  private val checkInDao: HabitCheckInDao,
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
    checkInDao.deleteForHabit(id)
    habitDao.delete(id)
    tableChangeNotifier.notify(Table.HabitCheckIn)
    tableChangeNotifier.notify(Table.Habit)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all habits")
    habitDao.getAllIds().forEach { checkInDao.deleteForHabit(it) }
    habitDao.deleteAll()
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

  override fun observeCheckIns(habitId: String, from: LocalDate, to: LocalDate): Flow<List<HabitCheckIn>> =
    checkInDao.observeRange(habitId, from.toString(), to.toString()).map { list -> list.map { it.toDomain() } }

  override suspend fun getCheckIns(habitId: String, from: LocalDate, to: LocalDate): List<HabitCheckIn> {
    Logger.d(TAG, "Get check-ins for habit: $habitId, $from..$to")
    return checkInDao.getRange(habitId, from.toString(), to.toString()).map { it.toDomain() }
  }

  override suspend fun getCheckIn(habitId: String, date: LocalDate): HabitCheckIn? {
    Logger.d(TAG, "Get check-in for habit: $habitId, date=$date")
    return checkInDao.get(habitId, date.toString())?.toDomain()
  }

  override suspend fun saveCheckIn(checkIn: HabitCheckIn) {
    Logger.d(TAG, "Save check-in: ${checkIn.habitId}, ${checkIn.date}")
    checkInDao.insert(checkIn.toEntity())
    tableChangeNotifier.notify(Table.HabitCheckIn)
  }

  override suspend fun deleteCheckIn(habitId: String, date: LocalDate) {
    Logger.d(TAG, "Delete check-in: $habitId, $date")
    checkInDao.delete(habitId, date.toString())
    tableChangeNotifier.notify(Table.HabitCheckIn)
  }

  override suspend fun deleteCheckIns(habitId: String) {
    Logger.d(TAG, "Delete all check-ins for habit: $habitId")
    checkInDao.deleteForHabit(habitId)
    tableChangeNotifier.notify(Table.HabitCheckIn)
  }

  companion object {
    private const val TAG = "HabitRepository"
  }
}
