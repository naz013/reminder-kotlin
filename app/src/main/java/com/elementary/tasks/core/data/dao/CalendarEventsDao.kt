package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.CalendarEvent

@Dao
interface CalendarEventsDao {

  @Query("SELECT * FROM CalendarEvent")
  fun all(): List<CalendarEvent>

  @Query("SELECT eventId FROM CalendarEvent")
  fun eventIds(): List<Long>

  @Query("SELECT * FROM CalendarEvent")
  fun loadAll(): LiveData<List<CalendarEvent>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(calendarEvent: CalendarEvent)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg calendarEvents: CalendarEvent)

  @Delete
  fun delete(calendarEvent: CalendarEvent)

  @Query("SELECT * FROM CalendarEvent WHERE uuId=:id")
  fun loadByKey(id: String): LiveData<CalendarEvent>

  @Query("SELECT * FROM CalendarEvent WHERE uuId=:id")
  fun getByKey(id: String): CalendarEvent?

  @Query("SELECT * FROM CalendarEvent WHERE reminderId=:id")
  fun getByReminder(id: String): List<CalendarEvent>

  @Query("DELETE FROM CalendarEvent WHERE uuId=:id")
  fun deleteById(id: String)
}
