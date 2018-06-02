package com.elementary.tasks.core.data.dao;

import com.elementary.tasks.core.data.models.Reminder;

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
public interface ReminderDao {

    @Query("SELECT * FROM Reminder WHERE uniqueId=:id")
    LiveData<Reminder> loadById(int id);

    @Query("SELECT * FROM Reminder WHERE noteId=:key")
    LiveData<Reminder> loadByNoteKey(String key);

    @Query("SELECT * FROM Reminder WHERE uniqueId=:id")
    Reminder getById(int id);

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed")
    LiveData<List<Reminder>> loadType(boolean active, boolean removed);

    @Query("SELECT * FROM Reminder WHERE isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime AND eventTime<:toTime")
    List<Reminder> getActiveInRange(boolean removed, String fromTime, String toTime);

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed")
    List<Reminder> getAll(boolean active, boolean removed);

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed AND type IN (:types)")
    List<Reminder> getAllTypes(boolean active, boolean removed, int[] types);

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed AND type IN (:types)")
    LiveData<List<Reminder>> loadAllTypes(boolean active, boolean removed, int[] types);

    @Query("SELECT * FROM Reminder WHERE isActive=:active AND isRemoved=:removed AND eventTime!=0 AND eventTime>=:fromTime AND eventTime<:toTime")
    List<Reminder> getAllTypesInRange(boolean active, boolean removed, String fromTime, String toTime);

    @Insert(onConflict = REPLACE)
    int insert(Reminder reminder);

    @Insert(onConflict = REPLACE)
    void insertAll(Reminder... reminder);

    @Delete
    void delete(Reminder reminder);

    @Delete
    void deleteAll(Reminder... reminder);
}
