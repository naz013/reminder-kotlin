package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages

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

    @Transaction
    @Query("SELECT * FROM Note")
    fun all(): List<NoteWithImages>

    @Transaction
    @Query("SELECT * FROM Note")
    fun loadAll(): LiveData<List<NoteWithImages>>

    @Insert(onConflict = REPLACE)
    fun insert(note: Note)

    @Delete
    fun delete(note: Note)

    @Transaction
    @Query("SELECT * FROM Note WHERE `key`=:id")
    fun loadById(id: String): LiveData<NoteWithImages>

    @Transaction
    @Query("SELECT * FROM Note WHERE `key`=:id")
    fun getById(id: String): NoteWithImages?

    @Transaction
    @Query("SELECT * FROM ImageFile WHERE noteId=:id")
    fun getImages(id: String): List<ImageFile>

    @Insert(onConflict = REPLACE)
    fun insert(imageFile: ImageFile)

    @Insert(onConflict = REPLACE)
    fun insertAll(notes: List<ImageFile>)

    @Delete
    fun delete(imageFile: ImageFile)
}
