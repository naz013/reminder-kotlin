package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.UsedTimeEntity

@Dao
internal interface UsedTimeDao {

  @Query("SELECT * FROM UsedTime ORDER BY useCount DESC")
  fun getAll(): List<UsedTimeEntity>

  @Query("SELECT * FROM UsedTime WHERE useCount > 1 ORDER BY useCount DESC LIMIT :limit")
  fun getFirst(limit: Int): List<UsedTimeEntity>

  @Query("SELECT * FROM UsedTime WHERE timeString=:timeString")
  fun getByTimeString(timeString: String): UsedTimeEntity?

  @Query("SELECT * FROM UsedTime WHERE timeMills=:timeMills")
  fun getByTimeMills(timeMills: Long): UsedTimeEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(usedTime: UsedTimeEntity)

  @Query("DELETE FROM UsedTime WHERE id=:id")
  fun delete(id: Long)

  @Query("DELETE FROM UsedTime")
  fun deleteAll()
}
