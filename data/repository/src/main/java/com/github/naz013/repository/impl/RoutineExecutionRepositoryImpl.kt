package com.github.naz013.repository.impl

import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RoutineExecutionRepository
import com.github.naz013.repository.dao.RoutineExecutionDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.entity.toEpochMillisUtc
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

internal class RoutineExecutionRepositoryImpl(
  private val dao: RoutineExecutionDao,
  private val tableChangeNotifier: TableChangeNotifier
) : RoutineExecutionRepository {

  private val table = Table.RoutineExecution

  override suspend fun save(record: RoutineExecutionRecord) {
    Logger.d(TAG, "Save routine execution: ${record.id}")
    dao.insert(record.toEntity())
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAll(): List<RoutineExecutionRecord> {
    Logger.d(TAG, "Get all routine executions")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getByRoutineId(routineId: String): List<RoutineExecutionRecord> {
    Logger.d(TAG, "Get routine executions by routineId: $routineId")
    return dao.getByRoutineId(routineId).map { it.toDomain() }
  }

  override suspend fun getByDateRange(from: LocalDate, to: LocalDate): List<RoutineExecutionRecord> {
    Logger.d(TAG, "Get routine executions by date range: $from - $to")
    val fromMillis = from.atTime(LocalTime.MIN).toEpochMillisUtc()
    val toMillis = to.atTime(LocalTime.MAX).toEpochMillisUtc()
    return dao.getByDateRange(fromMillis, toMillis).map { it.toDomain() }
  }

  override suspend fun deleteByRoutineId(routineId: String) {
    Logger.d(TAG, "Delete routine executions by routineId: $routineId")
    dao.deleteByRoutineId(routineId)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all routine executions")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "RoutineExecutionRepository"
  }
}
