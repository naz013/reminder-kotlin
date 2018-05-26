package com.elementary.tasks.core.controller;

import android.content.Context;

import com.elementary.tasks.ReminderApp;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.services.PermanentReminderReceiver;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;

import javax.inject.Inject;

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
    @Inject
    private Context mContext;
    @Inject
    private AppDb mDb;

    public Context getContext() {
        return mContext;
    }

    public Reminder getReminder() {
        return mReminder;
    }

    protected AppDb getDb() {
        return mDb;
    }

    public EventManager(Reminder reminder) {
        ReminderApp.getAppComponent().inject(this);
        this.mReminder = reminder;
    }

    protected void save() {
        mDb.reminderDao().insert(mReminder);
        UpdatesHelper.getInstance(getContext()).updateWidget();
        if (Prefs.getInstance(getContext()).isSbNotificationEnabled()) {
            Notifier.updateReminderPermanent(getContext(), PermanentReminderReceiver.ACTION_SHOW);
        }
    }
}
