package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.elementary.tasks.core.data.models.CalendarEvent

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
interface CalendarEventsDao {

    @Query("SELECT * FROM CalendarEvent")
    fun all(): List<CalendarEvent>

    @Query("SELECT eventId FROM CalendarEvent")
    fun eventIds(): List<Long>

    @Query("SELECT * FROM CalendarEvent")
    fun loadAll(): LiveData<List<CalendarEvent>>

    @Insert(onConflict = REPLACE)
    fun insert(calendarEvent: CalendarEvent)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg calendarEvents: CalendarEvent)

    @Delete
    fun delete(calendarEvent: CalendarEvent)

    @Query("SELECT * FROM CalendarEvent WHERE uuId=:id")
    fun loadByKey(id: String): LiveData<CalendarEvent>

    @Query("SELECT * FROM CalendarEvent WHERE uuId=:id")
    fun getByKey(id: String): CalendarEvent?

    @Query("SELECT * FROM CalendarEvent WHERE reminderId=:id")
    fun getByReminder(id: Int): List<CalendarEvent>
}
