package com.elementary.tasks.core.app_widgets;

import android.app.AlarmManager;
import android.content.Context;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Reminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

public class WidgetDataProvider {

    public enum WidgetType {
        BIRTHDAY,
        REMINDER
    }

    private List<Item> data;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private int hour, minute;
    private boolean isFeature;
    private Context mContext;

    public WidgetDataProvider(Context context){
        this.mContext = context;
        data = new ArrayList<>();
    }

    public void setTime(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public void setFeature(boolean isFeature){
        this.isFeature = isFeature;
    }

    public List<Item> getData(){
        return data;
    }

    public Item getItem(int position){
        return data.get(position);
    }

    public boolean hasReminder(int day, int month, int year){
        boolean res = false;
        for (Item item : data){
            if (res) break;
            int mDay = item.getDay();
            int mMonth = item.getMonth();
            int mYear = item.getYear();
            WidgetType type = item.getType();
            res = mDay == day && mMonth == month && mYear == year && type == WidgetType.REMINDER;
        }
        return res;
    }

    public boolean hasBirthday(int day, int month){
        boolean res = false;
        for (Item item : data){
            int mDay = item.getDay();
            int mMonth = item.getMonth();
            WidgetType type = item.getType();
            if (mDay == day && mMonth == month && type == WidgetType.BIRTHDAY) {
                res = true;
                break;
            }
        }
        return res;
    }

    public void fillArray(){
        data.clear();
        loadBirthdays();
        loadReminders();
    }

    public void loadReminders(){
        List<Reminder> reminderItems = RealmDb.getInstance().getEnabledReminders();
        for (Reminder item : reminderItems) {
            int mType = item.getType();
            long eventTime = item.getDateTime();
            if (!Reminder.isGpsType(item.getType()) && eventTime > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(eventTime);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                int mMonth = calendar.get(Calendar.MONTH);
                int mYear = calendar.get(Calendar.YEAR);
                if (eventTime > 0) {
                    data.add(new Item(mDay, mMonth, mYear, WidgetType.REMINDER));
                }
                long repeatTime = item.getRepeatInterval();
                long limit = item.getRepeatLimit();
                long count = item.getEventCount();
                boolean isLimited = limit > 0;
                if (isFeature) {
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTimeInMillis(eventTime);
                    if (Reminder.isBase(mType, Reminder.BY_WEEK)) {
                        long days = 0;
                        long max = Configs.MAX_DAYS_COUNT;
                        if (isLimited) max = limit - count;
                        List<Integer> list = item.getWeekdays();
                        do {
                            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                            int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                            if (list.get(weekDay - 1) == 1) {
                                int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                int sMonth = calendar1.get(Calendar.MONTH);
                                int sYear = calendar1.get(Calendar.YEAR);
                                days++;
                                data.add(new Item(sDay, sMonth, sYear, WidgetType.REMINDER));
                            }
                        } while (days < max);
                    } else if (Reminder.isBase(mType, Reminder.BY_MONTH)) {
                        long days = 0;
                        long max = Configs.MAX_DAYS_COUNT;
                        if (isLimited) max = limit - count;
                        do {
                            item.setEventTime(TimeUtil.getGmtFromDateTime(eventTime));
                            eventTime = TimeCount.getInstance(mContext).getNextMonthDayTime(item);
                            calendar1.setTimeInMillis(eventTime);
                            days++;
                            int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int sMonth = calendar1.get(Calendar.MONTH);
                            int sYear = calendar1.get(Calendar.YEAR);
                            data.add(new Item(sDay, sMonth, sYear, WidgetType.REMINDER));
                        } while (days < max);
                    } else {
                        if (repeatTime == 0) continue;
                        long days = 0;
                        long max = Configs.MAX_DAYS_COUNT;
                        if (isLimited) max = limit - count;
                        do {
                            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + repeatTime);
                            mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            mMonth = calendar1.get(Calendar.MONTH);
                            mYear = calendar1.get(Calendar.YEAR);
                            days++;
                            data.add(new Item(mDay, mMonth, mYear, WidgetType.REMINDER));
                        } while (days < max);
                    }
                }
            }
        }
    }

    public void loadBirthdays(){
        List<BirthdayItem> list = RealmDb.getInstance().getAllBirthdays();
        for (BirthdayItem item : list) {
            Date date = null;
            try {
                date = format.parse(item.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date != null) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(date);
                int bDay = calendar1.get(Calendar.DAY_OF_MONTH);
                int bMonth = calendar1.get(Calendar.MONTH);
                calendar1.setTimeInMillis(System.currentTimeMillis());
                calendar1.set(Calendar.MONTH, bMonth);
                calendar1.set(Calendar.DAY_OF_MONTH, bDay);
                calendar1.set(Calendar.HOUR_OF_DAY, hour);
                calendar1.set(Calendar.MINUTE, minute);
                data.add(new Item(bDay, bMonth, 0, WidgetType.BIRTHDAY));
            }
        }
    }

    public static class Item {
        private int day;
        private int month;
        private int year;
        private WidgetType type;

        public Item(int day, int month, int year, WidgetType type){
            this.day = day;
            this.month = month;
            this.year = year;
            this.type = type;
        }

        public int getYear(){
            return year;
        }

        public void setYear(int year){
            this.year = year;
        }

        public int getMonth(){
            return month;
        }

        public void setMonth(int month){
            this.month = month;
        }

        public int getDay(){
            return day;
        }

        public void setDay(int day){
            this.day = day;
        }

        public WidgetType getType(){
            return type;
        }
    }
}
