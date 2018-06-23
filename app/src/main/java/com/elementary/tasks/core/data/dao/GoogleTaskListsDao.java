package com.elementary.tasks.core.data.dao;

import com.elementary.tasks.core.data.models.GoogleTaskList;

import java.util.List;

import androidx.annotation.Nullable;
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
public interface GoogleTaskListsDao {

    @Query("SELECT * FROM GoogleTaskList")
    LiveData<List<GoogleTaskList>> loadAll();

    @Query("SELECT * FROM GoogleTaskList")
    List<GoogleTaskList> getAll();

    @Query("SELECT * FROM GoogleTaskList WHERE def=1")
    LiveData<GoogleTaskList> loadDefault();

    @Nullable
    @Query("SELECT * FROM GoogleTaskList WHERE def=1")
    GoogleTaskList getDefault();

    @Insert(onConflict = REPLACE)
    void insert(GoogleTaskList googleTaskList);

    @Insert(onConflict = REPLACE)
    void insertAll(GoogleTaskList... googleTaskLists);

    @Delete
    void delete(GoogleTaskList googleTaskList);

    @Query("SELECT * FROM GoogleTaskList WHERE listId=:id")
    LiveData<GoogleTaskList> loadById(String id);

    @Nullable
    @Query("SELECT * FROM GoogleTaskList WHERE listId=:id")
    GoogleTaskList getById(String id);

    @Query("DELETE FROM GoogleTaskList")
    void deleteAll();
}
