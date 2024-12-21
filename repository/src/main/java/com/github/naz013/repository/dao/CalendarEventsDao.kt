package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.CalendarEventEntity

@Dao
internal interface CalendarEventsDao {

  @Query("SELECT * FROM CalendarEvent")
  fun all(): List<CalendarEventEntity>

  @Query("SELECT eventId FROM CalendarEvent")
  fun eventIds(): List<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(calendarEvent: CalendarEventEntity)

  @Query("SELECT * FROM CalendarEvent WHERE uuId=:id")
  fun getByKey(id: String): CalendarEventEntity?

  @Query("SELECT * FROM CalendarEvent WHERE reminderId=:id")
  fun getByReminder(id: String): List<CalendarEventEntity>

  @Query("DELETE FROM CalendarEvent WHERE uuId=:id")
  fun deleteById(id: String)
}
