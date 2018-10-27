package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.Note
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy.REPLACE
import com.elementary.tasks.core.data.models.TmpNote

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
interface NotesDao {

    @Query("SELECT * FROM Note")
    fun all(): List<Note>

    @Query("SELECT * FROM Note")
    fun loadAll(): LiveData<List<Note>>

    @Insert(onConflict = REPLACE)
    fun insert(note: Note)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg notes: Note)

    @Delete
    fun delete(note: Note)

    @Delete
    fun delete(list: List<Note>)

    @Query("SELECT * FROM Note WHERE `key`=:id")
    fun loadById(id: String): LiveData<Note>

    @Query("SELECT * FROM Note WHERE `key`=:id")
    fun getById(id: String): Note?

    @Insert(onConflict = REPLACE)
    fun insert(tmpNote: TmpNote)

    @Delete
    fun delete(tmpNote: TmpNote)

    @Query("SELECT * FROM TmpNote LIMIT 1")
    fun getEditedImage(): TmpNote?
}
