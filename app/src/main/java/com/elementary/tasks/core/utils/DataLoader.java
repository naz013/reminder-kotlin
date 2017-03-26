package com.elementary.tasks.core.utils;

import android.os.Handler;

import com.elementary.tasks.core.interfaces.RealmCallback;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.List;

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

public class DataLoader {

    private static Handler handler = new Handler();

    public static void loadActiveReminder(String group, int type, RealmCallback<List<Reminder>> callback) {
        RealmDb.getInstance().getActiveReminders(group, type, result -> handler.post(() -> callback.onDataLoaded(result)));
    }

    public static void loadActiveReminder(RealmCallback<List<Reminder>> callback) {
        RealmDb.getInstance().getActiveReminders(result -> handler.post(() -> callback.onDataLoaded(result)));
    }

    public static void loadArchivedReminder(RealmCallback<List<Reminder>> callback) {
        RealmDb.getInstance().getArchivedReminders(result -> handler.post(() -> callback.onDataLoaded(result)));
    }

    public static void loadArchivedReminder(String group, int type, RealmCallback<List<Reminder>> callback) {
        RealmDb.getInstance().getArchivedReminders(group, type, result -> handler.post(() -> callback.onDataLoaded(result)));
    }
}
