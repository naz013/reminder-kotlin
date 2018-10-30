package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.elementary.tasks.core.data.models.Reminder

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Dao
interface ReminderDao {

    @Query("SELECT * FROM Reminder")
    fun all(): List<Reminder>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE uuId=:id AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun loadById(id: String): LiveData<Reminder>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE noteId=:key AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun loadByNoteKey(key: String): LiveData<Reminder>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE uuId=:id AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getById(id: String): Reminder?

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE Reminder.isRemoved=:removed AND ReminderGroup.groupUuId=Reminder.groupUuId ORDER BY Reminder.isActive DESC, Reminder.eventTime ASC")
    fun loadByRemoved(removed: Boolean): LiveData<List<Reminder>>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun loadType(active: Boolean, removed: Boolean): LiveData<List<Reminder>>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime AND eventTime<:toTime AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getActiveInRange(removed: Boolean, fromTime: String, toTime: String): List<Reminder>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getAll(active: Boolean, removed: Boolean): List<Reminder>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND type IN (:types) AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getAllTypes(active: Boolean, removed: Boolean, types: IntArray): List<Reminder>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND type IN (:types) AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun loadAllTypes(active: Boolean, removed: Boolean, types: IntArray): LiveData<List<Reminder>>

    @Transaction
    @Query("SELECT * FROM Reminder, ReminderGroup WHERE isActive=:active AND isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime AND eventTime<:toTime AND ReminderGroup.groupUuId=Reminder.groupUuId")
    fun getAllTypesInRange(active: Boolean, removed: Boolean, fromTime: String, toTime: String): List<Reminder>

    @Insert(onConflict = REPLACE)
    fun insert(reminder: Reminder)

    @Insert(onConflict = REPLACE)
    fun insertAll(reminders: Iterable<Reminder>)

    @Delete
    fun delete(reminder: Reminder)

    @Delete
    fun deleteAll(vararg reminder: Reminder)
}
