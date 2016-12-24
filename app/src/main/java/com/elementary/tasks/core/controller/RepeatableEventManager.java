package com.elementary.tasks.core.controller;

import android.content.Context;

import com.elementary.tasks.core.services.AlarmReceiver;
import com.elementary.tasks.core.services.DelayReceiver;
import com.elementary.tasks.core.services.RepeatNotificationReceiver;
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

abstract class RepeatableEventManager implements EventControl {

    protected Reminder mReminder;
    protected Context mContext;

    RepeatableEventManager(Reminder reminder, Context context) {
        this.mReminder = reminder;
        this.mContext = context;
    }

    protected void save() {
        RealmDb.getInstance().saveObject(mReminder);
    }

    @Override
    public boolean stop() {
        new AlarmReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        new DelayReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        new RepeatNotificationReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        mReminder.setActive(false);
        save();
        return true;
    }
}
