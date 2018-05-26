package com.elementary.tasks.core.controller;

import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;

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

class DateEvent extends RepeatableEventManager {

    DateEvent(Reminder reminder) {
        super(reminder);
    }

    @Override
    public boolean start() {
        if (TimeCount.isCurrent(getReminder().getEventTime())) {
            getReminder().setActive(true);
            super.save();
            super.enableReminder();
            super.export();
        }
        return true;
    }

    @Override
    public boolean skip() {
        return false;
    }

    @Override
    public boolean next() {
        getReminder().setDelay(0);
        if (canSkip()) {
            long time = calculateTime(false);
            getReminder().setEventTime(TimeUtil.getGmtFromDateTime(time));
            getReminder().setEventCount(getReminder().getEventCount() + 1);
            return start();
        } else {
            return stop();
        }
    }

    @Override
    public boolean onOff() {
        if (isActive()) {
            return stop();
        } else {
            super.save();
            return start();
        }
    }

    @Override
    public boolean isActive() {
        return getReminder().isActive();
    }

    @Override
    public boolean canSkip() {
        return isRepeatable() && (getReminder().getRepeatLimit() == -1 || getReminder().getRepeatLimit() - getReminder().getEventCount() - 1 > 0);
    }

    @Override
    public boolean isRepeatable() {
        return getReminder().getRepeatInterval() > 0;
    }

    @Override
    public void setDelay(int delay) {
        if (delay == 0) {
            next();
            return;
        }
        getReminder().setDelay(delay);
        super.save();
        super.setDelay(delay);
    }

    @Override
    public long calculateTime(boolean isNew) {
        return TimeCount.getInstance(getContext()).generateDateTime(getReminder().getEventTime(), getReminder().getRepeatInterval());
    }
}