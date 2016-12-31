package com.elementary.tasks.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.missed_calls.CallItem;
import com.elementary.tasks.missed_calls.MissedCallDialogActivity;

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

public class MissedCallReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String number = intent.getStringExtra(Constants.INTENT_ID);
        Intent service = new Intent(context, MissedCallReceiver.class);
        context.startService(service);
        Intent resultIntent = new Intent(context, MissedCallDialogActivity.class);
        resultIntent.putExtra(Constants.INTENT_ID, number);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(resultIntent);
    }

    public void setAlarm(Context context, CallItem item) {
        Intent intent = new Intent(context, MissedCallReceiver.class);
        intent.putExtra(Constants.INTENT_ID, item.getNumber());
        int time = Prefs.getInstance(context).getMissedReminderTime();
        long mills = 1000 * 60;
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, item.getUniqueId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Module.isMarshmallow()) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + (time * mills), time * mills, alarmIntent);
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + (time * mills), time * mills, alarmIntent);
        }
    }

    public void cancelAlarm(Context context, int id) {
        Intent intent = new Intent(context, MissedCallReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}
