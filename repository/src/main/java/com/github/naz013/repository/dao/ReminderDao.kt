package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.naz013.repository.entity.ReminderEntity
import com.github.naz013.repository.entity.ReminderWithGroupEntity

@Dao
internal interface ReminderDao {

  @Transaction
  @Query(
    """SELECT * FROM Reminder
        WHERE LOWER(summary) LIKE '%' || :query || '%'
        ORDER BY isActive DESC, eventTime ASC"""
  )
  fun search(query: String): List<ReminderWithGroupEntity>

  @Query("SELECT * FROM Reminder")
  fun getAll(): List<ReminderEntity>

  @Transaction
  @Query("SELECT * FROM Reminder WHERE uuId=:id")
  fun getById(id: String): ReminderWithGroupEntity?

  @Transaction
  @Query("SELECT * FROM Reminder WHERE noteId=:key")
  fun getByNoteKey(key: String): List<ReminderWithGroupEntity>

  @Transaction
  @Query(
    """SELECT * FROM Reminder
        WHERE isRemoved=:removed
        AND LOWER(summary) LIKE '%' || :query || '%'
        ORDER BY isActive DESC, eventTime ASC"""
  )
  fun searchBySummaryAndRemovedStatus(query: String, removed: Boolean = false): List<ReminderWithGroupEntity>

  @Transaction
  @Query(
    """SELECT * FROM Reminder
        WHERE isRemoved=:removed
        ORDER BY isActive DESC, eventTime ASC"""
  )
  fun getByRemovedStatus(removed: Boolean = false): List<ReminderWithGroupEntity>

  @Transaction
  @Query(
    """SELECT * FROM Reminder
        WHERE isRemoved=:removed
        AND isActive=:active"""
  )
  fun getAll(active: Boolean, removed: Boolean): List<ReminderWithGroupEntity>

  @Transaction
  @Query(
    """SELECT * FROM Reminder
        WHERE isRemoved=:removed
        AND eventTime!=''
        AND eventTime>=:fromTime
        AND eventTime<:toTime"""
  )
  fun getActiveInRange(removed: Boolean, fromTime: String, toTime: String): List<ReminderWithGroupEntity>

  @Transaction
  @Query(
    """SELECT * FROM Reminder
        WHERE isRemoved=:removed
        AND isActive=:active
        AND eventTime!=''
        AND eventTime>=:fromTime
        AND eventTime<:toTime"""
  )
  fun getAllTypesInRange(
    active: Boolean,
    removed: Boolean,
    fromTime: String,
    toTime: String
  ): List<ReminderWithGroupEntity>

  @Transaction
  @Query(
    """SELECT * FROM Reminder
        WHERE isRemoved=:removed
        AND isActive=:active
        AND type IN (:types)"""
  )
  fun getAllTypes(active: Boolean, removed: Boolean, types: IntArray): List<ReminderWithGroupEntity>

  @Transaction
  @Query(
    """SELECT * FROM Reminder
        WHERE isRemoved=:removed
        AND isActive=:active
        AND type IN (:types)
        AND LOWER(summary) LIKE '%' || :query || '%'
        ORDER BY isActive DESC, eventTime ASC"""
  )
  fun searchBySummaryAllTypes(
    query: String,
    active: Boolean,
    removed: Boolean,
    types: IntArray
  ): List<ReminderWithGroupEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(reminder: ReminderEntity)

  @Query("DELETE FROM Reminder WHERE uuId=:id")
  fun delete(id: String)

  @Query("DELETE FROM Reminder WHERE uuId IN (:ids)")
  fun deleteAll(ids: List<String>)

  @Query("DELETE FROM Reminder")
  fun deleteAll()

  @Query("UPDATE Reminder SET syncState=:state WHERE uuId=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT uuId FROM Reminder WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("SELECT uuId FROM Reminder")
  fun getAllIds(): List<String>
}
