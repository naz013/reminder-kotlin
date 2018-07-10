package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.GoogleTask
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
interface GoogleTasksDao {

    @get:Query("SELECT * FROM GoogleTask")
    val all: List<GoogleTask>

    @Query("SELECT * FROM GoogleTask")
    fun loadAll(): LiveData<List<GoogleTask>>

    @Query("SELECT * FROM GoogleTask WHERE listId=:listId")
    fun loadAllByList(listId: String): LiveData<List<GoogleTask>>

    @Query("SELECT * FROM GoogleTask WHERE listId=:listId AND status=:status")
    fun getAllByList(listId: String, status: String): List<GoogleTask>

    @Insert(onConflict = REPLACE)
    fun insert(googleTask: GoogleTask)

    @Insert(onConflict = REPLACE)
    fun insertAll(googleTasks: List<GoogleTask>)

    @Delete
    fun delete(googleTask: GoogleTask)

    @Query("SELECT * FROM GoogleTask WHERE taskId=:id")
    fun loadById(id: String): LiveData<GoogleTask>

    @Query("SELECT * FROM GoogleTask WHERE taskId=:id")
    fun getById(id: String): GoogleTask?

    @Query("SELECT * FROM GoogleTask WHERE uuId=:id")
    fun getByReminderId(id: String): GoogleTask?

    @Delete
    fun deleteAll(googleTasks: List<GoogleTask>)

    @Query("DELETE FROM GoogleTask")
    fun deleteAll()

    @Query("DELETE FROM GoogleTask WHERE listId=:listId")
    fun deleteAll(listId: String)
}
