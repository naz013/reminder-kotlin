package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.Reminder
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy.REPLACE

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

    @Query("SELECT * FROM Reminder WHERE uniqueId=:id")
    fun loadById(id: Int): LiveData<Reminder>

    @Query("SELECT * FROM Reminder WHERE noteId=:key")
    fun loadByNoteKey(key: String): LiveData<Reminder>

    @Query("SELECT * FROM Reminder WHERE uuId=:uuId")
    fun getByUuId(uuId: String): Reminder?

    @Query("SELECT * FROM Reminder WHERE uniqueId=:id")
    fun getById(id: Int): Reminder?

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed")
    fun loadType(active: Boolean, removed: Boolean): LiveData<List<Reminder>>

    @Query("SELECT * FROM Reminder WHERE isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime AND eventTime<:toTime")
    fun getActiveInRange(removed: Boolean, fromTime: String, toTime: String): List<Reminder>

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed")
    fun getAll(active: Boolean, removed: Boolean): List<Reminder>

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed AND type IN (:types)")
    fun getAllTypes(active: Boolean, removed: Boolean, types: IntArray): List<Reminder>

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed AND type IN (:types)")
    fun loadAllTypes(active: Boolean, removed: Boolean, types: IntArray): LiveData<List<Reminder>>

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime AND eventTime<:toTime")
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
