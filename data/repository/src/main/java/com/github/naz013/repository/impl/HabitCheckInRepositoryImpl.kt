package com.github.naz013.repository.impl

import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.logging.Logger
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.dao.HabitCheckInDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDate

internal class HabitCheckInRepositoryImpl(
  private val dao: HabitCheckInDao,
  private val tableChangeNotifier: TableChangeNotifier
) : HabitCheckInRepository {

  override fun observeRange(habitId: String, from: LocalDate, to: LocalDate): Flow<List<HabitCheckIn>> =
    dao.observeRange(habitId, from.toString(), to.toString()).map { list -> list.map { it.toDomain() } }

  override suspend fun getRange(habitId: String, from: LocalDate, to: LocalDate): List<HabitCheckIn> {
    Logger.d(TAG, "Get check-ins for habit: $habitId, $from..$to")
    return dao.getRange(habitId, from.toString(), to.toString()).map { it.toDomain() }
  }

  override suspend fun get(habitId: String, date: LocalDate): HabitCheckIn? {
    Logger.d(TAG, "Get check-in for habit: $habitId, date=$date")
    return dao.get(habitId, date.toString())?.toDomain()
  }

  override suspend fun save(checkIn: HabitCheckIn) {
    Logger.d(TAG, "Save check-in: ${checkIn.habitId}, ${checkIn.date}")
    dao.insert(checkIn.toEntity())
    tableChangeNotifier.notify(Table.HabitCheckIn)
  }

  override suspend fun delete(habitId: String, date: LocalDate) {
    Logger.d(TAG, "Delete check-in: $habitId, $date")
    dao.delete(habitId, date.toString())
    tableChangeNotifier.notify(Table.HabitCheckIn)
  }

  override suspend fun deleteAllForHabit(habitId: String) {
    Logger.d(TAG, "Delete all check-ins for habit: $habitId")
    dao.deleteForHabit(habitId)
    tableChangeNotifier.notify(Table.HabitCheckIn)
  }

  companion object {
    private const val TAG = "HabitCheckInRepository"
  }
}
