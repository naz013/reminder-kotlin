package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.github.naz013.repository.entity.PomodoroSessionEntity

@Dao
internal interface PomodoroSessionDao {

  @Insert(onConflict = REPLACE)
  fun insert(entity: PomodoroSessionEntity)

  @Query("SELECT * FROM PomodoroSession WHERE startedAt BETWEEN :fromMillis AND :toMillis ORDER BY startedAt")
  fun getByDateRange(fromMillis: Long, toMillis: Long): List<PomodoroSessionEntity>

  @Query("SELECT * FROM PomodoroSession WHERE linkedReminderId=:reminderId ORDER BY startedAt DESC")
  fun getByReminderId(reminderId: String): List<PomodoroSessionEntity>

  @Query("SELECT COALESCE(SUM(actualFocusSeconds), 0) FROM PomodoroSession WHERE linkedReminderId=:reminderId")
  fun getTotalFocusSecondsByReminderId(reminderId: String): Int

  @Query("DELETE FROM PomodoroSession")
  fun deleteAll()
}
