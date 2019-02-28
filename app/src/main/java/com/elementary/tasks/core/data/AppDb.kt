package com.elementary.tasks.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elementary.tasks.core.data.dao.*
import com.elementary.tasks.core.data.models.*

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
@Database(entities = [
    Reminder::class,
    CalendarEvent::class,
    ReminderGroup::class,
    MissedCall::class,
    Note::class,
    Place::class,
    GoogleTaskList::class,
    GoogleTask::class,
    UsedTime::class,
    Birthday::class,
    ImageFile::class,
    SmsTemplate::class
], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun reminderGroupDao(): ReminderGroupDao
    abstract fun missedCallsDao(): MissedCallsDao
    abstract fun smsTemplatesDao(): SmsTemplatesDao
    abstract fun placesDao(): PlacesDao
    abstract fun calendarEventsDao(): CalendarEventsDao
    abstract fun notesDao(): NotesDao
    abstract fun birthdaysDao(): BirthdaysDao
    abstract fun googleTaskListsDao(): GoogleTaskListsDao
    abstract fun googleTasksDao(): GoogleTasksDao
    abstract fun usedTimeDao(): UsedTimeDao

    companion object {

        private var INSTANCE: AppDb? = null

        fun getAppDatabase(context: Context): AppDb {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, AppDb::class.java, "app_db")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
