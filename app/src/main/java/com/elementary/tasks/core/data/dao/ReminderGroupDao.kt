package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.ReminderGroup
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
interface ReminderGroupDao {

    @Query("SELECT * FROM ReminderGroup LIMIT 1")
    fun defaultGroup(): ReminderGroup?

    @Query("SELECT * FROM ReminderGroup")
    fun all(): List<ReminderGroup>

    @Query("SELECT * FROM ReminderGroup")
    fun loadAll(): LiveData<List<ReminderGroup>>

    @Query("SELECT * FROM ReminderGroup LIMIT 1")
    fun loadDefault(): LiveData<ReminderGroup>

    @Insert(onConflict = REPLACE)
    fun insert(reminderGroup: ReminderGroup)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg reminderGroups: ReminderGroup)

    @Delete
    fun delete(reminderGroup: ReminderGroup)

    @Query("SELECT * FROM ReminderGroup WHERE groupUuId=:id")
    fun loadById(id: String): LiveData<ReminderGroup>

    @Query("SELECT * FROM ReminderGroup WHERE groupUuId=:id")
    fun getById(id: String): ReminderGroup?
}
