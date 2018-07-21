package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.GoogleTaskList
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
interface GoogleTaskListsDao {

    @Query("SELECT * FROM GoogleTaskList")
    fun all(): List<GoogleTaskList>

    @Query("SELECT * FROM GoogleTaskList WHERE def=1")
    fun defaultGoogleTaskList(): GoogleTaskList?

    @Query("SELECT * FROM GoogleTaskList")
    fun loadAll(): LiveData<List<GoogleTaskList>>

    @Query("SELECT * FROM GoogleTaskList WHERE def=1")
    fun loadDefault(): LiveData<GoogleTaskList>

    @Insert(onConflict = REPLACE)
    fun insert(googleTaskList: GoogleTaskList)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg googleTaskLists: GoogleTaskList)

    @Delete
    fun delete(googleTaskList: GoogleTaskList)

    @Query("SELECT * FROM GoogleTaskList WHERE listId=:id")
    fun loadById(id: String): LiveData<GoogleTaskList>

    @Query("SELECT * FROM GoogleTaskList WHERE listId=:id")
    fun getById(id: String): GoogleTaskList?

    @Query("DELETE FROM GoogleTaskList")
    fun deleteAll()
}
