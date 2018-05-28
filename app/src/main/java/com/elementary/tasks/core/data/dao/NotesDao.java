package com.elementary.tasks.core.data.dao;

import com.elementary.tasks.core.data.models.Note;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * Copyright 2018 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Dao
public interface NotesDao {

    @Query("SELECT * FROM Note")
    LiveData<List<Note>> loadAll();

    @Query("SELECT * FROM Note")
    List<Note> getAll();

    @Insert(onConflict = REPLACE)
    void insert(Note note);

    @Insert(onConflict = REPLACE)
    void insertAll(Note... notes);

    @Delete
    void delete(Note note);

    @Delete
    void delete(List<Note> list);

    @Query("SELECT * FROM Note WHERE `key`=:id")
    LiveData<Note> loadById(String id);
}
