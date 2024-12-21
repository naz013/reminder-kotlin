package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.naz013.repository.entity.ReminderEntity

private const val byIdQuery = """
    SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
    FROM Reminder AS reminder
    JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
    WHERE reminder.uuId=:id"""

@Dao
internal interface ReminderDao {

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE LOWER(Reminder.summary) LIKE '%' || :query || '%'
        ORDER BY reminder.isActive DESC, reminder.eventTime ASC"""
  )
  fun search(query: String): List<ReminderEntity>

  @Query("SELECT * FROM Reminder")
  fun getAll(): List<ReminderEntity>

  @Transaction
  @Query(byIdQuery)
  fun getById(id: String): ReminderEntity?

  @Transaction
  @Query(
    """
    SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
    FROM Reminder AS reminder
    JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
    WHERE reminder.noteId=:key"""
  )
  fun getByNoteKey(key: String): List<ReminderEntity>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND LOWER(Reminder.summary) LIKE '%' || :query || '%'
        ORDER BY reminder.isActive DESC, reminder.eventTime ASC"""
  )
  fun searchBySummaryAndRemovedStatus(query: String, removed: Boolean = false): List<ReminderEntity>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        ORDER BY reminder.isActive DESC, reminder.eventTime ASC"""
  )
  fun getByRemovedStatus(removed: Boolean = false): List<ReminderEntity>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active"""
  )
  fun getAll(active: Boolean, removed: Boolean): List<ReminderEntity>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.eventTime!=''
        AND reminder.eventTime>=:fromTime
        AND reminder.eventTime<:toTime"""
  )
  fun getActiveInRange(removed: Boolean, fromTime: String, toTime: String): List<ReminderEntity>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active
        AND reminder.eventTime!=''
        AND reminder.eventTime>=:fromTime
        AND reminder.eventTime<:toTime"""
  )
  fun getAllTypesInRange(
    active: Boolean,
    removed: Boolean,
    fromTime: String,
    toTime: String
  ): List<ReminderEntity>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active
        AND reminder.type IN (:types)"""
  )
  fun getAllTypes(active: Boolean, removed: Boolean, types: IntArray): List<ReminderEntity>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active
        AND reminder.type IN (:types)
        AND LOWER(Reminder.summary) LIKE '%' || :query || '%'
        ORDER BY reminder.isActive DESC, reminder.eventTime ASC"""
  )
  fun searchBySummaryAllTypes(
    query: String,
    active: Boolean,
    removed: Boolean,
    types: IntArray
  ): List<ReminderEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(reminder: ReminderEntity)

  @Query("DELETE FROM Reminder WHERE uuId=:id")
  fun delete(id: String)

  @Query("DELETE FROM Reminder WHERE uuId IN (:ids)")
  fun deleteAll(ids: List<String>)

  @Query("DELETE FROM Reminder")
  fun deleteAll()
}
