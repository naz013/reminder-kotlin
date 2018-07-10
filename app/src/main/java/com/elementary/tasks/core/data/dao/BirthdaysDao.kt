package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.Birthday
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
interface BirthdaysDao {

    @get:Query("SELECT * FROM Birthday")
    val all: List<Birthday>

    @Query("SELECT * FROM Birthday WHERE uniqueId=:id")
    fun loadById(id: Int): LiveData<Birthday>

    @Query("SELECT * FROM Birthday WHERE uniqueId=:id")
    fun getById(id: Int): Birthday?

    @Query("SELECT * FROM Birthday")
    fun loadAll(): LiveData<List<Birthday>>

    @Query("SELECT * FROM Birthday WHERE dayMonth=:dayMonth")
    fun getAll(dayMonth: String): List<Birthday>

    @Insert(onConflict = REPLACE)
    fun insert(birthday: Birthday): Int

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg birthdays: Birthday)

    @Delete
    fun delete(birthday: Birthday)

    @Delete
    fun deleteAll(vararg birthdays: Birthday)
}
