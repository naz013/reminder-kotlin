package com.elementary.tasks.core.controller;

import android.content.Context;

import com.elementary.tasks.core.services.AlarmReceiver;
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

public class TimerEvent extends EventManager {

    public TimerEvent(Reminder reminder, Context context) {
        super(reminder, context);
    }

    @Override
    public void start() {

        new AlarmReceiver().enableReminder(mContext, mReminder.getUuId());
    }

    @Override
    public void stop() {
        new AlarmReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        RealmDb.getInstance().saveObject(mReminder.setActive(false));
    }

    @Override
    public void pause() {

    }

    @Override
    public void skip() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void next() {

    }

    @Override
    public void onOff() {
        if (isActive()) {
            stop();
        } else {
            start();
        }
    }

    @Override
    public boolean isActive() {
        return mReminder.isActive();
    }

    @Override
    public boolean canSkip() {
        return mReminder.getRepeatLimit() != -1 && mReminder.getEventCount() < mReminder.getRepeatLimit();
    }

    @Override
    public boolean isRepeatable() {
        return mReminder.getRepeatInterval() > 0;
    }
}
