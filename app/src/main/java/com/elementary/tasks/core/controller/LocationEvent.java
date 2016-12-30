package com.elementary.tasks.core.controller;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.elementary.tasks.core.services.GeolocationService;
import com.elementary.tasks.core.services.PositionDelayReceiver;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.List;

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

class LocationEvent extends EventManager {

    LocationEvent(Reminder reminder, Context context) {
        super(reminder, context);
    }

    @Override
    public boolean start() {
        mReminder.setActive(true);
        super.save();
        if (!TextUtils.isEmpty(mReminder.getEventTime())) {
            new PositionDelayReceiver().setDelay(mContext, mReminder.getUuId());
            return true;
        } else {
            if (!SuperUtil.isServiceRunning(mContext, GeolocationService.class)) {
                mContext.startService(new Intent(mContext, GeolocationService.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            return true;
        }
    }

    @Override
    public boolean stop() {
        new PositionDelayReceiver().cancelDelay(mContext, mReminder.getUniqueId());
        RealmDb.getInstance().saveObject(mReminder.setActive(false));
        Notifier.hideNotification(mContext, mReminder.getUniqueId());
        mReminder.setActive(false);
        super.save();
        stopTracking();
        return true;
    }

    private void stopTracking() {
        List<Reminder> list = RealmDb.getInstance().getEnabledReminders();
        if (list.size() == 0) {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
        }
        boolean hasActive = false;
        for (Reminder item : list) {
            if (Reminder.isGpsType(item.getType())) {
                if (!TextUtils.isEmpty(item.getEventTime())) {
                    if (TimeCount.isCurrent(item.getEventTime())) {
                        hasActive = !item.isNotificationShown();
                    }
                } else {
                    hasActive = !item.isNotificationShown();
                }
            }
        }
        if (!hasActive) {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
        }
    }

    @Override
    public boolean pause() {
        new PositionDelayReceiver().cancelDelay(mContext, mReminder.getUniqueId());
        stopTracking();
        return true;
    }

    @Override
    public boolean skip() {
        return false;
    }

    @Override
    public boolean resume() {
        new PositionDelayReceiver().setDelay(mContext, mReminder.getUuId());
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
            mReminder.setLocked(false);
            mReminder.setNotificationShown(false);
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

    }

    @Override
    public long calculateTime(boolean isNew) {
        return TimeCount.getInstance(mContext).generateDateTime(mReminder.getEventTime(), mReminder.getRepeatInterval());
    }
}
