package com.elementary.tasks.core.controller;

import android.content.Context;
import android.text.TextUtils;

import com.elementary.tasks.core.services.AlarmReceiver;
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

class ShoppingEvent extends RepeatableEventManager {

    ShoppingEvent(Reminder reminder, Context context) {
        super(reminder, context);
    }

    @Override
    public boolean start() {
        getReminder().setActive(true);
        super.save();
        if (!TextUtils.isEmpty(getReminder().getEventTime())) {
            new AlarmReceiver().enableReminder(getContext(), getReminder().getUuId());
            return true;
        }
        return true;
    }

    @Override
    public boolean skip() {
        return false;
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
            return start();
        }
    }

    @Override
    public boolean isActive() {
        return getReminder().isActive();
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
        getReminder().setDelay(delay);
        super.save();
        new AlarmReceiver().enableDelay(getContext(), getReminder().getUniqueId(), delay, getReminder().getUuId());
    }

    @Override
    public long calculateTime(boolean isNew) {
        return TimeCount.getInstance(getContext()).generateDateTime(getReminder().getEventTime(), getReminder().getRepeatInterval());
    }
}