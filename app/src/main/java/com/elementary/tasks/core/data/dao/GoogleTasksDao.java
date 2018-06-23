package com.elementary.tasks.core.data.dao;

import com.elementary.tasks.core.data.models.GoogleTask;

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
public interface GoogleTasksDao {

    @Query("SELECT * FROM GoogleTask")
    LiveData<List<GoogleTask>> loadAll();

    @Query("SELECT * FROM GoogleTask")
    List<GoogleTask> getAll();

    @Query("SELECT * FROM GoogleTask WHERE listId=:listId")
    LiveData<List<GoogleTask>> loadAllByList(String listId);

    @Query("SELECT * FROM GoogleTask WHERE listId=:listId AND status=:status")
    List<GoogleTask> getAllByList(String listId, String status);

    @Insert(onConflict = REPLACE)
    void insert(GoogleTask googleTask);

    @Insert(onConflict = REPLACE)
    void insertAll(List<GoogleTask> googleTasks);

    @Delete
    void delete(GoogleTask googleTask);

    @Query("SELECT * FROM GoogleTask WHERE taskId=:id")
    LiveData<GoogleTask> loadById(String id);

    @Nullable
    @Query("SELECT * FROM GoogleTask WHERE taskId=:id")
    GoogleTask getById(String id);

    @Nullable
    @Query("SELECT * FROM GoogleTask WHERE uuId=:id")
    GoogleTask getByReminderId(String id);

    @Delete
    void deleteAll(List<GoogleTask> googleTasks);

    @Query("DELETE FROM GoogleTask")
    void deleteAll();
}
