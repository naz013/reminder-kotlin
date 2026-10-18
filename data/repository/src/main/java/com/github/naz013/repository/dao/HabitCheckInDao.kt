package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.HabitCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface HabitCheckInDao {

  @Query(
    "SELECT * FROM HabitCheckIn WHERE habitId=:habitId AND date>=:from AND date<=:to ORDER BY date ASC"
  )
  fun observeRange(habitId: String, from: String, to: String): Flow<List<HabitCheckInEntity>>

  @Query(
    "SELECT * FROM HabitCheckIn WHERE habitId=:habitId AND date>=:from AND date<=:to ORDER BY date ASC"
  )
  fun getRange(habitId: String, from: String, to: String): List<HabitCheckInEntity>

  @Query("SELECT * FROM HabitCheckIn WHERE habitId=:habitId AND date=:date")
  fun get(habitId: String, date: String): HabitCheckInEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(checkIn: HabitCheckInEntity)

  @Query("DELETE FROM HabitCheckIn WHERE habitId=:habitId AND date=:date")
  fun delete(habitId: String, date: String)

  @Query("DELETE FROM HabitCheckIn WHERE habitId=:habitId")
  fun deleteForHabit(habitId: String)
}
