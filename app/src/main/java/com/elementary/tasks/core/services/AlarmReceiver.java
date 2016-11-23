package com.elementary.tasks.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.ReminderDialogActivity;
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

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("----ON_RECEIVE-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        String id = intent.getStringExtra(Constants.INTENT_ID);
        Intent service = new Intent(context, AlarmReceiver.class);
        context.startService(service);
        Reminder item = RealmDb.getInstance().getReminder(id);
        int type = item.getType();
        if (Reminder.isType(type, Reminder.BY_TIME)) {
            if (!TimeCount.getInstance(context).isRange(item.getHours(), item.getFrom(), item.getTo()))
                start(context, id);
            else {
//                Reminder.update(context, id);
//                UpdatesHelper.getInstance(context).updateWidget();
            }
        } else start(context, id);
    }

    private void start(Context context, String id) {
        Intent resultIntent = new Intent(context, ReminderDialogActivity.class);
        resultIntent.putExtra(Constants.INTENT_ID, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(resultIntent);
    }

    public void enableReminder(Context context, String uuId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.INTENT_ID, uuId);
        Reminder item = RealmDb.getInstance().getReminder(uuId);
        long due = 0;
        if (item != null) {
            due = TimeUtil.getDateTimeFromGmt(item.getEventTime());
            /*if (due < System.currentTimeMillis()) {
                if (type != null) {
                    if (type.startsWith(Constants.TYPE_WEEKDAY) ||
                            type.startsWith(Constants.TYPE_MONTHDAY)) {
                        due = new TimeCount(context).generateDateTime(type,
                                jRecurrence.getMonthday(), System.currentTimeMillis(), repeat,
                                jRecurrence.getWeekdays(), jModel.getCount(), 0);
                        jModel.setEventTime(due);
                        db.updateReminderTime(id, due);
                        db.updateCount(id, jModel.toJsonString());
                    }
                }
            }*/
        }
        Log.d(TAG, "enableReminder: " + due);
        if (due == 0) return;
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, item.getUniqueId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Module.isMarshmallow()) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, due, alarmIntent);
        }
    }

    public void cancelAlarm(Context context, int id) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) alarmMgr.cancel(alarmIntent);
    }
}
