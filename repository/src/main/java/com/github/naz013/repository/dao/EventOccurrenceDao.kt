package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.naz013.repository.entity.EventOccurrenceEntity

@Dao
internal interface EventOccurrenceDao {

  @Insert
  suspend fun insert(occurrence: EventOccurrenceEntity)

  @Query("SELECT * FROM EventOccurrence WHERE date BETWEEN :startDate AND :endDate ORDER BY date, time")
  suspend fun getByDateRange(startDate: Long, endDate: Long): List<EventOccurrenceEntity>

  @Query("SELECT * FROM EventOccurrence WHERE eventId = :eventId ORDER BY date, time")
  suspend fun getByEventId(eventId: String): List<EventOccurrenceEntity>

  @Query("DELETE FROM EventOccurrence WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM EventOccurrence WHERE eventId = :eventId")
  suspend fun deleteByEventId(eventId: String)

  @Query("DELETE FROM EventOccurrence")
  suspend fun deleteAll()
}
