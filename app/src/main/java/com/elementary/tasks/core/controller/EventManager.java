package com.elementary.tasks.core.controller;

import android.content.Context;
import android.content.Intent;

import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.services.PermanentReminderService;
import com.elementary.tasks.core.utils.Prefs;
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

public abstract class EventManager implements EventControl {

    private Reminder mReminder;
    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public Reminder getReminder() {
        return mReminder;
    }

    public EventManager(Reminder reminder, Context context) {
        this.mReminder = reminder;
        this.mContext = context;
    }

    protected void save() {
        RealmDb.getInstance().saveObject(mReminder);
        UpdatesHelper.getInstance(mContext).updateWidget();
        if (Prefs.getInstance(mContext).isSbNotificationEnabled()) {
            mContext.startService(new Intent(mContext, PermanentReminderService.class)
                    .setAction(PermanentReminderService.ACTION_SHOW));
        }
    }
}
