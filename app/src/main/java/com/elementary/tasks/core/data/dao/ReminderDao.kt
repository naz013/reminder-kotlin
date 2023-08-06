package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elementary.tasks.core.data.models.Reminder

private const val byIdQuery = """
    SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
    FROM Reminder AS reminder
    JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
    WHERE reminder.uuId=:id"""

private const val byNoteIdQuery = """
    SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
    FROM Reminder AS reminder
    JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
    WHERE reminder.noteId=:key"""

@Dao
interface ReminderDao {

  @Query("SELECT * FROM Reminder")
  fun getAll(): List<Reminder>

  @Transaction
  @Query(byIdQuery)
  fun loadById(id: String): LiveData<Reminder?>

  @Transaction
  @Query(byIdQuery)
  fun getById(id: String): Reminder?

  @Transaction
  @Query(byNoteIdQuery)
  fun loadByNoteKey(key: String): LiveData<Reminder>

  @Transaction
  @Query(byNoteIdQuery)
  fun getByNoteKey(key: String): Reminder?

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND LOWER(Reminder.summary) LIKE '%' || :query || '%'
        ORDER BY reminder.isActive DESC, reminder.eventTime ASC"""
  )
  fun searchBySummaryAndRemovedStatus(query: String, removed: Boolean = false): List<Reminder>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        ORDER BY reminder.isActive DESC, reminder.eventTime ASC"""
  )
  fun getByRemovedStatus(removed: Boolean = false): List<Reminder>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active"""
  )
  fun loadType(active: Boolean, removed: Boolean): LiveData<List<Reminder>>

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
  fun getActiveInRange(removed: Boolean, fromTime: String, toTime: String): List<Reminder>

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
  ): List<Reminder>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active"""
  )
  fun getAll(active: Boolean, removed: Boolean): List<Reminder>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active
        AND reminder.type IN (:types)"""
  )
  fun getAllTypes(active: Boolean, removed: Boolean, types: IntArray): List<Reminder>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active
        AND reminder.type IN (:types)"""
  )
  fun loadAllTypes(active: Boolean, removed: Boolean, types: IntArray): LiveData<List<Reminder>>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE reminder.isRemoved=:removed
        AND reminder.isActive=:active
        AND reminder.eventTime!=''
        AND reminder.eventTime>=:fromTime
        AND reminder.eventTime<:toTime
        ORDER BY reminder.eventTime ASC"""
  )
  fun loadAllTypesInRange(
    active: Boolean = true,
    removed: Boolean = false,
    fromTime: String,
    toTime: String
  ): LiveData<List<Reminder>>

  @Transaction
  @Query(
    """SELECT reminder.*, g.groupTitle, g.groupUuId, g.groupColor
        FROM Reminder AS reminder
        JOIN ReminderGroup AS g ON reminder.groupUuId = g.groupUuId
        WHERE (reminder.isActive=:active AND reminder.isRemoved=:removed)
        AND (reminder.eventTime=='' OR (reminder.eventTime>=:fromTime AND reminder.eventTime<:toTime))
        ORDER BY reminder.eventTime ASC"""
  )
  fun loadAllTypesInRangeWithPermanent(
    active: Boolean = true,
    removed: Boolean = false,
    fromTime: String,
    toTime: String
  ): LiveData<List<Reminder>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(reminder: Reminder)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(reminders: Iterable<Reminder>)

  @Delete
  fun delete(reminder: Reminder)

  @Query("DELETE FROM Reminder WHERE uuId=:id")
  fun delete(id: String)

  @Delete
  fun deleteAll(reminders: Iterable<Reminder>)

  @Query("DELETE FROM Reminder")
  fun deleteAll()
}
