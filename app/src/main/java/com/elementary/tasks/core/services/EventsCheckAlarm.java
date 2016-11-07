package com.elementary.tasks.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;

import java.util.ArrayList;
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

public class EventsCheckAlarm extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, EventsCheckAlarm.class);
        context.startService(service);
        new CheckEventsAsync(context).execute();
    }

    public void setAlarm(Context context){
        Intent intent1 = new Intent(context, EventsCheckAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 1111, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int interval = Prefs.getInstance(context).getAutoCheckInterval();
        if (Module.isMarshmallow()) alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
        else alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
    }

    public void cancelAlarm(Context context) {
        Integer i = (int) (long) 1100;
        Intent intent = new Intent(context, EventsCheckAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    private class CheckEventsAsync extends AsyncTask<Void, Void, Void> {
        Context context;

        public CheckEventsAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            CalendarUtils cm = CalendarUtils.getInstance(context);
            int calID = Prefs.getInstance(context).getEventsCalendar();
            ArrayList<CalendarUtils.EventItem> eventItems = cm.getEvents(calID);
            if (eventItems != null && eventItems.size() > 0){
//                DataBase db = new DataBase(context);
//                db.open();
//                Cursor c = db.getCalendarEvents();
//                ArrayList<Long> ids = new ArrayList<>();
//                if (c != null && c.moveToFirst()){
//                    do {
//                        long eventId = c.getLong(c.getColumnIndex(Constants.COLUMN_EVENT_ID));
//                        ids.add(eventId);
//                    } while (c.moveToNext());
//                }
//                if (c != null) c.close();
//                for (CalendarHelper.EventItem item : eventItems){
//                    long itemId = item.getId();
//                    if (!ids.contains(itemId)) {
//                        String rrule = item.getRrule();
//                        int repeat = 0;
//                        if (rrule != null && !rrule.matches("")) {
//                            try {
//                                RecurrenceRule rule = new RecurrenceRule(rrule);
//                                int interval = rule.getInterval();
//                                Freq freq = rule.getFreq();
//                                if (freq == Freq.HOURLY || freq == Freq.MINUTELY || freq == Freq.SECONDLY) {
//                                } else {
//                                    if (freq == Freq.WEEKLY) repeat = interval * 7;
//                                    else if (freq == Freq.MONTHLY) repeat = interval * 30;
//                                    else if (freq == Freq.YEARLY) repeat = interval * 365;
//                                    else repeat = interval;
//                                }
//                            } catch (InvalidRecurrenceRuleException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        String summary = item.getTitle();
//                        String uuID = SyncHelper.generateID();
//                        String categoryId = GroupHelper.getInstance(context).getDefaultUuId();
//                        long due = item.getDtStart() + (repeat * AlarmManager.INTERVAL_DAY);
//                        JRecurrence jRecurrence = new JRecurrence(0, repeat, -1, null, 0);
//                        JsonModel jsonModel = new JsonModel(summary, Constants.TYPE_REMINDER, categoryId, uuID, due,
//                                due, jRecurrence, null, null);
//                        long id = new DateType(context, Constants.TYPE_REMINDER).save(new ReminderItem(jsonModel));
//                        db.addCalendarEvent(null, id, item.getId());
//                    }
//                }
//                db.close();
            }
            return null;
        }
    }
}
