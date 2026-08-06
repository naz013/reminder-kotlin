package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Query
import com.github.naz013.repository.entity.ReminderEntity

@Dao
internal interface ReminderDao {
  @Query("SELECT * FROM Reminder")
  fun getAll(): List<ReminderEntity>
}
