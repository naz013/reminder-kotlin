package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.ReminderGroup
import androidx.lifecycle.LiveData
import androidx.room.*

import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface ReminderGroupDao {

    @Transaction
    @Query("SELECT * FROM ReminderGroup WHERE isDefaultGroup=:isDef LIMIT 1")
    fun defaultGroup(isDef: Boolean = true): ReminderGroup?

    @Transaction
    @Query("SELECT * FROM ReminderGroup ORDER BY isDefaultGroup DESC")
    fun all(): List<ReminderGroup>

    @Transaction
    @Query("SELECT * FROM ReminderGroup ORDER BY isDefaultGroup DESC")
    fun loadAll(): LiveData<List<ReminderGroup>>

    @Transaction
    @Query("SELECT * FROM ReminderGroup WHERE isDefaultGroup='true' ORDER BY isDefaultGroup LIMIT 1")
    fun loadDefault(): LiveData<ReminderGroup>

    @Transaction
    @Insert(onConflict = REPLACE)
    fun insert(reminderGroup: ReminderGroup)

    @Insert(onConflict = REPLACE)
    fun insertAll(reminderGroups: List<ReminderGroup>)

    @Delete
    fun delete(reminderGroup: ReminderGroup)

    @Query("SELECT * FROM ReminderGroup WHERE groupUuId=:id")
    fun loadById(id: String): LiveData<ReminderGroup>

    @Query("SELECT * FROM ReminderGroup WHERE groupUuId=:id")
    fun getById(id: String): ReminderGroup?
}
