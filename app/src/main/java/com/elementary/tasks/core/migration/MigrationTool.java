package com.elementary.tasks.core.migration;

import android.content.Context;
import android.database.SQLException;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.SmsTemplate;
import com.elementary.tasks.core.data.models.Reminder;

/**
 * Copyright 2017 Nazar Suhovich
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

public class MigrationTool {

    public static void migrate(Context context) throws SQLException {
        DataBase db = new DataBase(context);
        db.open();
        RealmDb realmDb = RealmDb.getInstance();
        for (Group item : db.getAllGroups()) {
            realmDb.saveObject(item);
        }
        for (SmsTemplate item : db.getAllTemplates()) {
            realmDb.saveObject(item);
        }
        for (BirthdayItem item : db.getBirthdays()) {
            realmDb.saveObject(item);
        }
        db.close();
        NotesBase notesBase = new NotesBase(context);
        notesBase.open();
        for (Note item : notesBase.getNotes()) {
            realmDb.saveObject(item);
        }
        notesBase.close();
        RemindersBase base = new RemindersBase(context);
        base.open();
        for (Reminder reminder : base.queryAllReminders()) {
            realmDb.saveReminder(reminder, null);
        }
        base.close();
    }
}
