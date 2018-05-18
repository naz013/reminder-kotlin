package com.elementary.tasks.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.elementary.tasks.birthdays.CheckBirthdaysAsync;
import com.elementary.tasks.core.async.BackupTask;
import com.elementary.tasks.core.calendar.CalendarEvent;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.reminder.models.Reminder;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.Calendar;
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

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final int AUTO_SYNC_ID = Integer.MAX_VALUE - 1;
    private static final int BIRTHDAY_PERMANENT_ID = Integer.MAX_VALUE - 2;
    private static final int BIRTHDAY_CHECK_ID = Integer.MAX_VALUE - 4;
    private static final int EVENTS_CHECK_ID = Integer.MAX_VALUE - 5;

    private static final String ACTION_BIRTHDAY_PERMANENT = "com.elementary.alarm.BIRTHDAY_PERMANENT";
    private static final String ACTION_BIRTHDAY_AUTO = "com.elementary.alarm.BIRTHDAY_AUTO";
    private static final String ACTION_SYNC_AUTO = "com.elementary.alarm.SYNC_AUTO";
    private static final String ACTION_EVENTS_CHECK = "com.elementary.alarm.EVENTS_CHECK";

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.d(TAG, "onReceive: Action - " + action + ", time - " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true, true));
        if (action == null) return;
        Intent service = new Intent(context, AlarmReceiver.class);
        context.startService(service);
        switch (action) {
            case ACTION_SYNC_AUTO:
                new BackupTask(context).execute();
                break;
            case ACTION_EVENTS_CHECK:
                checkEvents(context);
                break;
            case ACTION_BIRTHDAY_AUTO:
                new CheckBirthdaysAsync(context).execute();
                break;
            case ACTION_BIRTHDAY_PERMANENT:
                if (Prefs.getInstance(context).isBirthdayPermanentEnabled()) {
                    Notifier.showBirthdayPermanent(context);
                }
                break;
        }
    }

    private void checkEvents(Context context) {
        if (Permissions.checkPermission(context, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            new CheckEventsAsync(context).execute();
        }
    }

    public void enableBirthdayPermanentAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_BIRTHDAY_PERMANENT);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_PERMANENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long currTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        while (currTime > time) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            time = calendar.getTimeInMillis();
        }
        if (Module.isMarshmallow()) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, alarmIntent);
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public void cancelBirthdayPermanentAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_BIRTHDAY_PERMANENT);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_PERMANENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    public void enableBirthdayCheckAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_BIRTHDAY_AUTO);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long currTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        while (currTime > time) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            time = calendar.getTimeInMillis();
        }
        if (Module.isMarshmallow()) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, alarmIntent);
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public void cancelBirthdayCheckAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_BIRTHDAY_AUTO);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    public void enableEventCheck(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_EVENTS_CHECK);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, EVENTS_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int interval = Prefs.getInstance(context).getAutoCheckInterval();
        if (Module.isMarshmallow()) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
        }
    }

    public void cancelEventCheck(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_EVENTS_CHECK);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, EVENTS_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    public void enableAutoSync(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_SYNC_AUTO);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, AUTO_SYNC_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;
        Calendar calendar = Calendar.getInstance();
        int interval = Prefs.getInstance(context).getAutoBackupInterval();
        calendar.setTimeInMillis(System.currentTimeMillis() + (AlarmManager.INTERVAL_HOUR * interval));
        if (Module.isMarshmallow()) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
        }
    }

    public void cancelAutoSync(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_SYNC_AUTO);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, AUTO_SYNC_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    private static class CheckEventsAsync extends AsyncTask<Void, Void, Void> {

        private Context mContext;

        CheckEventsAsync(Context context) {
            this.mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            long currTime = System.currentTimeMillis();
            int calID = Prefs.getInstance(mContext).getEventsCalendar();
            List<CalendarUtils.EventItem> eventItems = CalendarUtils.getEvents(mContext, calID);
            if (eventItems.size() > 0) {
                List<Long> list = RealmDb.getInstance().getCalendarEventsIds();
                for (CalendarUtils.EventItem item : eventItems) {
                    long itemId = item.getId();
                    if (!list.contains(itemId)) {
                        String rrule = item.getRrule();
                        long repeat = 0;
                        if (rrule != null && !rrule.matches("")) {
                            try {
                                RecurrenceRule rule = new RecurrenceRule(rrule);
                                int interval = rule.getInterval();
                                Freq freq = rule.getFreq();
                                if (freq == Freq.SECONDLY) repeat = interval * TimeCount.SECOND;
                                else if (freq == Freq.MINUTELY)
                                    repeat = interval * TimeCount.MINUTE;
                                else if (freq == Freq.HOURLY) repeat = interval * TimeCount.HOUR;
                                else if (freq == Freq.WEEKLY) repeat = interval * 7 * TimeCount.DAY;
                                else if (freq == Freq.MONTHLY)
                                    repeat = interval * 30 * TimeCount.DAY;
                                else if (freq == Freq.YEARLY)
                                    repeat = interval * 365 * TimeCount.DAY;
                                else repeat = interval * TimeCount.DAY;
                            } catch (InvalidRecurrenceRuleException e) {
                                e.printStackTrace();
                            }
                        }
                        String summary = item.getTitle();
                        GroupItem def = RealmDb.getInstance().getDefaultGroup();
                        String categoryId = "";
                        if (def != null) {
                            categoryId = def.getUuId();
                        }
                        Calendar calendar = Calendar.getInstance();
                        long dtStart = item.getDtStart();
                        calendar.setTimeInMillis(dtStart);
                        if (dtStart >= currTime) {
                            saveReminder(itemId, summary, dtStart, repeat, categoryId);
                        } else {
                            if (repeat > 0) {
                                do {
                                    calendar.setTimeInMillis(dtStart + (repeat * AlarmManager.INTERVAL_DAY));
                                    dtStart = calendar.getTimeInMillis();
                                } while (dtStart < currTime);
                                saveReminder(itemId, summary, dtStart, repeat, categoryId);
                            }
                        }
                    }
                }
            }
            return null;
        }

        private void saveReminder(long itemId, String summary, long dtStart, long repeat, String categoryId) {
            Reminder reminder = new Reminder();
            reminder.setType(Reminder.BY_DATE);
            reminder.setRepeatInterval(repeat);
            reminder.setGroupUuId(categoryId);
            reminder.setSummary(summary);
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(dtStart));
            reminder.setStartTime(TimeUtil.getGmtFromDateTime(dtStart));
            RealmDb.getInstance().saveReminder(reminder, () -> {
                EventControl control = EventControlFactory.getController(mContext, reminder);
                control.start();
            });
            CalendarEvent event = new CalendarEvent(reminder.getUuId(), summary, itemId);
            RealmDb.getInstance().saveObject(event);
        }
    }
}
