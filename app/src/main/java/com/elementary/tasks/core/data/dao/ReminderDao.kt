package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.elementary.tasks.core.data.models.Reminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM Reminder")
    fun all(): List<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE uuId=:id AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun loadById(id: String): LiveData<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE noteId=:key AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun loadByNoteKey(key: String): LiveData<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE uuId=:id AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getById(id: String): Reminder?

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE Reminder.isRemoved=:removed AND ReminderGroup.groupUuId=Reminder.groupUuId ORDER BY Reminder.isActive DESC, Reminder.eventTime ASC")
    fun loadNotRemoved(removed: Boolean = false): LiveData<List<Reminder>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE Reminder.isRemoved=:removed AND ReminderGroup.groupUuId=Reminder.groupUuId ORDER BY Reminder.isActive DESC, Reminder.eventTime ASC")
    fun getNotRemoved(removed: Boolean = false): List<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun loadType(active: Boolean, removed: Boolean): LiveData<List<Reminder>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime AND eventTime<:toTime AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getActiveInRange(removed: Boolean, fromTime: String, toTime: String): List<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getAll(active: Boolean, removed: Boolean): List<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND type IN (:types) AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getAllTypes(active: Boolean, removed: Boolean, types: IntArray): List<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND type IN (:types) AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun loadAllTypes(active: Boolean, removed: Boolean, types: IntArray): LiveData<List<Reminder>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime AND eventTime<:toTime AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getAllTypesInRange(active: Boolean, removed: Boolean, fromTime: String, toTime: String): List<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("""SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active
        AND isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime
        AND eventTime<:toTime AND ReminderGroup.groupUuId=Reminder.groupUuId LIMIT :limit""")
    fun loadAllTypesInRange(active: Boolean = true, removed: Boolean = false, limit: Int = 3,
                            fromTime: String, toTime: String): LiveData<List<Reminder>>

    @Insert(onConflict = REPLACE)
    fun insert(reminder: Reminder)

    @Insert(onConflict = REPLACE)
    fun insertAll(reminders: Iterable<Reminder>)

    @Delete
    fun delete(reminder: Reminder)

    @Query("DELETE FROM Reminder WHERE uuId=:id")
    fun delete(id: String)

    @Delete
    fun deleteAll(reminders: Iterable<Reminder>)

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE eventState=:status")
    fun getAllByStatus(status: Int = 10): List<Reminder>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE eventState=:status")
    fun loadAllByStatus(status: Int = 10): LiveData<List<Reminder>>
}
