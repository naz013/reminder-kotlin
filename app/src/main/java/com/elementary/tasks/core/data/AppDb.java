package com.elementary.tasks.core.data;

import android.content.Context;

import com.elementary.tasks.core.data.dao.BirthdaysDao;
import com.elementary.tasks.core.data.dao.CalendarEventsDao;
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao;
import com.elementary.tasks.core.data.dao.GoogleTasksDao;
import com.elementary.tasks.core.data.dao.GroupDao;
import com.elementary.tasks.core.data.dao.MissedCallsDao;
import com.elementary.tasks.core.data.dao.NotesDao;
import com.elementary.tasks.core.data.dao.PlacesDao;
import com.elementary.tasks.core.data.dao.ReminderDao;
import com.elementary.tasks.core.data.dao.SmsTemplatesDao;
import com.elementary.tasks.core.data.models.CalendarEvent;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.data.models.GoogleTaskList;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.MissedCall;
import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.data.models.SmsTemplate;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

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
@Database(entities = {
        Reminder.class,
        CalendarEvent.class,
        Group.class,
        MissedCall.class,
        Note.class,
        Place.class,
        GoogleTaskList.class,
        GoogleTask.class,
        SmsTemplate.class
}, version = 1, exportSchema = false)
public abstract class AppDb extends RoomDatabase {

    private static AppDb INSTANCE;

    public abstract ReminderDao reminderDao();

    public abstract GroupDao groupDao();

    public abstract MissedCallsDao missedCallsDao();

    public abstract SmsTemplatesDao smsTemplatesDao();

    public abstract PlacesDao placesDao();

    public abstract CalendarEventsDao calendarEventsDao();

    public abstract NotesDao notesDao();

    public abstract BirthdaysDao birthdaysDao();

    public abstract GoogleTaskListsDao googleTaskListsDao();

    public abstract GoogleTasksDao googleTasksDao();

    public static AppDb getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDb.class, "app_db")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
