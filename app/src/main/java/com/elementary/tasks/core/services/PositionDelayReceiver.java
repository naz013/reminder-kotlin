package com.elementary.tasks.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
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

public class PositionDelayReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SuperUtil.isServiceRunning(context, GeolocationService.class)) {
            context.startService(new Intent(context, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public boolean setDelay(Context context, String id) {
        Reminder item = RealmDb.getInstance().getReminder(id);
        long startTime = TimeUtil.getDateTimeFromGmt(item.getEventTime());
        if (startTime == 0 || startTime < System.currentTimeMillis()) {
            return false;
        }
        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, item.getUniqueId(), intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Module.isMarshmallow()) alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, alarmIntent);
        else alarmMgr.set(AlarmManager.RTC_WAKEUP, startTime, alarmIntent);
        return true;
    }

    public void cancelDelay(Context context, int id) {
        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}
