package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.github.naz013.repository.entity.EventHistoryEntity

@Dao
internal interface EventHistoryDao {

  @Insert(onConflict = REPLACE)
  suspend fun insert(entity: EventHistoryEntity)

  @Insert(onConflict = REPLACE)
  suspend fun insertAll(entities: List<EventHistoryEntity>)

  @Query("SELECT * FROM EventHistory WHERE date BETWEEN :startDate AND :endDate ORDER BY date, time")
  suspend fun getByDateRange(startDate: Long, endDate: Long): List<EventHistoryEntity>

  @Query("SELECT * FROM EventHistory WHERE eventId = :eventId ORDER BY date, time")
  suspend fun getByEventId(eventId: String): List<EventHistoryEntity>

  @Query("SELECT * FROM EventHistory WHERE date = :date AND time BETWEEN :startTime AND :endTime ORDER BY time")
  suspend fun getByDateAndTimeRange(date: Long, startTime: Int, endTime: Int): List<EventHistoryEntity>

  @Query("DELETE FROM EventHistory WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM EventHistory WHERE eventId = :eventId")
  suspend fun deleteByEventId(eventId: String)

  @Query("DELETE FROM EventHistory")
  suspend fun deleteAll()
}
