package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.GoogleCalendarEventEntity

@Dao
internal interface GoogleCalendarEventDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(event: GoogleCalendarEventEntity)

  @Query("SELECT * FROM GoogleCalendarEvent WHERE uuId=:id")
  fun getByKey(id: String): GoogleCalendarEventEntity?

  @Query("SELECT * FROM GoogleCalendarEvent WHERE deviceEventId=:deviceEventId")
  fun getByDeviceEventId(deviceEventId: Long): GoogleCalendarEventEntity?

  @Query("SELECT * FROM GoogleCalendarEvent WHERE isDismissed=0")
  fun visible(): List<GoogleCalendarEventEntity>

  @Query("SELECT deviceEventId FROM GoogleCalendarEvent")
  fun knownDeviceEventIds(): List<Long>

  @Query("UPDATE GoogleCalendarEvent SET isDismissed=1 WHERE uuId=:id")
  fun markDismissed(id: String)

  @Query("DELETE FROM GoogleCalendarEvent WHERE deviceEventId=:deviceEventId")
  fun deleteByDeviceEventId(deviceEventId: Long)

  @Query("DELETE FROM GoogleCalendarEvent")
  fun deleteAll()
}
