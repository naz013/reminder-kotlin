package com.elementary.tasks.birthdays;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;

import com.elementary.tasks.core.calendar.Events;
import com.elementary.tasks.core.calendar.FlextHelper;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Reminder;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import hirondelle.date4j.DateTime;

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

public class EventsDataProvider {

    private static final String TAG = "EventsDataProvider";

    private Context mContext;
    private boolean isReminder = false;
    private boolean isFeature = false;

    private HashMap<DateTime, Events> map = new HashMap<>();

    public EventsDataProvider(Context mContext, boolean isReminder, boolean isFeature){
        this.mContext = mContext;
        this.isReminder = isReminder;
        this.isFeature = isFeature;
        map = new HashMap<>();
    }

    private void setEvent(long eventTime, String summary, int color, Events.Type type) {
        DateTime key = FlextHelper.convertToDateTime(eventTime);
        if (map.containsKey(key)) {
            Events events = map.get(key);
            events.addEvent(summary, color, type);
            map.put(key, events);
        } else {
            Events events = new Events(summary, color, type);
            map.put(key, events);
        }
    }

    public HashMap<DateTime, Events> getEvents() {
        ThemeUtil cs = ThemeUtil.getInstance(mContext);
        int bColor = cs.getColor(cs.colorBirthdayCalendar());
        TimeCount timeCount = TimeCount.getInstance(mContext);
        if (isReminder) {
            int rColor = cs.getColor(cs.colorReminderCalendar());
            List<Reminder> reminders = RealmDb.getInstance().getEnabledReminders();
            for (Reminder item : reminders) {
                int mType = item.getType();
                String summary = item.getSummary();
                long eventTime = item.getDateTime();
                if (!Reminder.isGpsType(mType) && eventTime > 0) {
                    long repeatTime = item.getRepeatInterval();
                    long limit = item.getRepeatLimit();
                    long count = item.getEventCount();
                    boolean isLimited = limit > 0;
                    setEvent(eventTime, summary, rColor, Events.Type.REMINDER);
                    if (isFeature) {
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTimeInMillis(eventTime);
                        if (Reminder.isBase(mType, Reminder.BY_WEEK)) {
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            List<Integer> list = item.getWeekdays();
                            do {
                                calendar1.setTimeInMillis(calendar1.getTimeInMillis() +
                                        AlarmManager.INTERVAL_DAY);
                                eventTime = calendar1.getTimeInMillis();
                                int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                                if (list.get(weekDay - 1) == 1) {
                                    days++;
                                    setEvent(eventTime, summary, rColor, Events.Type.REMINDER);
                                }
                            } while (days < max);
                        } else if (Reminder.isBase(mType, Reminder.BY_MONTH)) {
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            do {
                                item.setEventTime(TimeUtil.getGmtFromDateTime(eventTime));
                                eventTime = timeCount.getNextMonthDayTime(item);
                                calendar1.setTimeInMillis(eventTime);
                                days++;
                                setEvent(eventTime, summary, rColor, Events.Type.REMINDER);
                            } while (days < max);
                        } else {
                            if (repeatTime == 0) continue;
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            do {
                                calendar1.setTimeInMillis(calendar1.getTimeInMillis() + repeatTime);
                                eventTime = calendar1.getTimeInMillis();
                                days++;
                                setEvent(eventTime, summary, rColor, Events.Type.REMINDER);
                            } while (days < max);
                        }
                    }
                }
            }
        }
        List<BirthdayItem> list = RealmDb.getInstance().getAllBirthdays();
        Log.d(TAG, "Count BD" + list.size());
        for (BirthdayItem item : list) {
            Date date = null;
            try {
                date = CheckBirthdaysAsync.dateFormat.parse(item.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int year = calendar.get(Calendar.YEAR);
            if (date != null) {
                try {
                    calendar.setTime(date);
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
                int i = -1;
                while (i < 2) {
                    calendar.set(Calendar.YEAR, year + i);
                    setEvent(calendar.getTimeInMillis(), item.getName(), bColor, Events.Type.BIRTHDAY);
                    i++;
                }
            }
        }
        return map;
    }
}
