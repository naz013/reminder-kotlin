package com.elementary.tasks.reminder;

import android.content.Context;

import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.reminder.models.Reminder;

/**
 * Copyright 2016 Nazar Suhovich
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

public class ReminderControl {

    private Context mContext;
    private static ReminderControl instance;

    private ReminderControl(Context context) {
        this.mContext = context;
    }

    public static ReminderControl getInstance(Context context) {
        if (instance == null) {
            instance = new ReminderControl(context);
        }
        return instance;
    }

    public boolean moveToTrash(Reminder reminder) {
        RealmDb.getInstance().deleteReminder(reminder.getUuId());
        return true;
    }
}
