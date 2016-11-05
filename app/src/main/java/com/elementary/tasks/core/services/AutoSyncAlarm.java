package com.elementary.tasks.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.elementary.tasks.core.async.SyncTask;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;

import java.util.Calendar;

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

public class AutoSyncAlarm extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, AutoSyncAlarm.class);
        context.startService(service);
        new SyncTask(context, null, true).execute();
    }

    public void setAlarm(Context context){
        Intent intent1 = new Intent(context, AutoSyncAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 1101, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        int interval = Prefs.getInstance(context).getAutoBackupInterval();
        calendar.setTimeInMillis(System.currentTimeMillis() + (AlarmManager.INTERVAL_HOUR * interval));
        if (Module.isMarshmallow())
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
        else alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AutoSyncAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 1101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}
