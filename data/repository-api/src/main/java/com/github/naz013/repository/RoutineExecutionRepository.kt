package com.github.naz013.repository

import com.github.naz013.domain.routine.RoutineExecutionRecord
import org.threeten.bp.LocalDate

interface RoutineExecutionRepository {
  suspend fun save(record: RoutineExecutionRecord)

  suspend fun getAll(): List<RoutineExecutionRecord>

  suspend fun getByRoutineId(routineId: String): List<RoutineExecutionRecord>

  suspend fun getByDateRange(from: LocalDate, to: LocalDate): List<RoutineExecutionRecord>

  suspend fun deleteByRoutineId(routineId: String)

  suspend fun deleteAll()
}
