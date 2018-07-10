package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.Group
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
interface GroupDao {

    @get:Query("SELECT * FROM `Group` LIMIT 1")
    val default: Group?

    @get:Query("SELECT * FROM `Group`")
    val all: List<Group>

    @Query("SELECT * FROM `Group`")
    fun loadAll(): LiveData<List<Group>>

    @Query("SELECT * FROM `Group` LIMIT 1")
    fun loadDefault(): LiveData<Group>

    @Insert(onConflict = REPLACE)
    fun insert(group: Group)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg groups: Group)

    @Delete
    fun delete(group: Group)

    @Query("SELECT * FROM `Group` WHERE uuId=:id")
    fun loadById(id: String): LiveData<Group>

    @Query("SELECT * FROM `Group` WHERE uuId=:id")
    fun getById(id: String): Group?
}
