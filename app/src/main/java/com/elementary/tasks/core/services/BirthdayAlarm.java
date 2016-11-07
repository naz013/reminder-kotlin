package com.elementary.tasks.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.TimeUtil;

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

public class BirthdayAlarm extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, BirthdayAlarm.class);
        context.startService(service);
        cancelAlarm(context);
        setAlarm(context);
//        Intent check = new Intent(context, CheckBirthdays.class);
//        context.startService(check);
    }

    public void setAlarm(Context context){
        Intent intent1 = new Intent(context, BirthdayAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 210, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String time = Prefs.getInstance(context).getBirthdayTime();
        if (Module.isMarshmallow()) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, TimeUtil.getBirthdayCalendar(time), alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, TimeUtil.getBirthdayCalendar(time), alarmIntent);
        }
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, BirthdayAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 210, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) alarmMgr.cancel(alarmIntent);
    }
}
