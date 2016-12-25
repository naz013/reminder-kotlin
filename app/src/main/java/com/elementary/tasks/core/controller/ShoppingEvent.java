package com.elementary.tasks.core.controller;

import android.content.Context;
import android.text.TextUtils;

import com.elementary.tasks.core.services.AlarmReceiver;
import com.elementary.tasks.core.services.DelayReceiver;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.TimeCount;
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

class ShoppingEvent extends EventManager {

    ShoppingEvent(Reminder reminder, Context context) {
        super(reminder, context);
    }

    @Override
    public boolean start() {
        if (!TextUtils.isEmpty(mReminder.getEventTime()) && TimeCount.isCurrent(mReminder.getEventTime())) {
            new AlarmReceiver().enableReminder(mContext, mReminder.getUuId());
            return true;
        }
        return true;
    }

    @Override
    public boolean stop() {
        Notifier.hideNotification(mContext, mReminder.getUniqueId());
        new AlarmReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        super.save();
        return true;
    }

    @Override
    public boolean pause() {
        new AlarmReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        return true;
    }

    @Override
    public boolean skip() {
        return false;
    }

    @Override
    public boolean resume() {
        new AlarmReceiver().enableReminder(mContext, mReminder.getUuId());
        return true;
    }

    @Override
    public boolean next() {
        return stop();
    }

    @Override
    public boolean onOff() {
        if (isActive()) {
            return stop();
        } else {
            mReminder.setActive(true);
            super.save();
            return start();
        }
    }

    @Override
    public boolean isActive() {
        return mReminder.isActive();
    }

    @Override
    public boolean canSkip() {
        return false;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public void setDelay(int delay) {
        if (delay == 0) {
            next();
            return;
        }
        mReminder.setDelay(delay);
        super.save();
        new DelayReceiver().setAlarm(mContext, mReminder.getUniqueId(), delay, mReminder.getUuId());
    }
}
