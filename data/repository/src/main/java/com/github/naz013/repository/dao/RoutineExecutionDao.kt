package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.github.naz013.repository.entity.RoutineExecutionEntity

@Dao
internal interface RoutineExecutionDao {

  @Insert(onConflict = REPLACE)
  fun insert(entity: RoutineExecutionEntity)

  @Query("SELECT * FROM RoutineExecution ORDER BY executedAt DESC")
  fun getAll(): List<RoutineExecutionEntity>

  @Query("SELECT * FROM RoutineExecution WHERE routineId=:routineId ORDER BY executedAt DESC")
  fun getByRoutineId(routineId: String): List<RoutineExecutionEntity>

  @Query("SELECT * FROM RoutineExecution WHERE executedAt BETWEEN :fromMillis AND :toMillis ORDER BY executedAt")
  fun getByDateRange(fromMillis: Long, toMillis: Long): List<RoutineExecutionEntity>

  @Query("DELETE FROM RoutineExecution WHERE routineId=:routineId")
  fun deleteByRoutineId(routineId: String)

  @Query("DELETE FROM RoutineExecution")
  fun deleteAll()
}
