package com.elementary.tasks.core.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.elementary.tasks.R;
import com.elementary.tasks.reminder.models.Reminder;

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

public class TimeCount {

    private static final String TAG = "TimeCount";
    public final static long SECOND = 1000;
    public final static long MINUTE = 60 * SECOND;
    public final static long HOUR = MINUTE * 60;
    public final static long HALF_DAY = HOUR * 12;
    public final static long DAY = HALF_DAY * 2;

    private Context mContext;
    private static TimeCount instance;

    private TimeCount(Context context) {
        this.mContext = context;
    }

    public static TimeCount getInstance(Context context) {
        if (instance == null) {
            instance = new TimeCount(context);
        }
        return instance;
    }

    public long generateNextTimer(Reminder reminder, boolean isNew) {
        List<Integer> hours = reminder.getHours();
        String fromHour = reminder.getFrom();
        String toHour = reminder.getTo();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (isNew) calendar.setTimeInMillis(calendar.getTimeInMillis() + reminder.getAfter());
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hours != null && hours.size() > 0) {
            while (hours.contains(mHour)) {
                calendar.setTimeInMillis(calendar.getTimeInMillis() + reminder.getRepeatInterval());
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
            }
            return calendar.getTimeInMillis();
        }
        long eventTime = calendar.getTimeInMillis();
        if (fromHour != null && toHour != null) {
            Date fromDate = TimeUtil.getDate(fromHour);
            Date toDate = TimeUtil.getDate(toHour);
            if (fromDate != null && toDate != null) {
                calendar.setTime(fromDate);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                long start = calendar.getTimeInMillis();
                calendar.setTime(toDate);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                long end = calendar.getTimeInMillis();
                while (isRange(eventTime, start, end)) {
                    eventTime = eventTime + reminder.getRepeatInterval();
                }
            }
        }
        return eventTime;
    }

    private boolean isRange(long time, long start, long end) {
        if (start > end) {
            return time >= start || time < end;
        } else {
            return time >= start && time <= end;
        }
    }

    public String[] getNextDateTime(long timeLong) {
        String date;
        String time;
        if (timeLong == 0) {
            date = null;
            time = null;
        } else {
            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(timeLong);
            Date mTime = cl.getTime();
            date = TimeUtil.dateFormat.format(mTime);
            time = TimeUtil.getTime(mTime, Prefs.getInstance(mContext).is24HourFormatEnabled());
        }
        return new String[]{date, time};
    }

    public long generateDateTime(String eventTime, long repeat, long delay) {
        if (TextUtils.isEmpty(eventTime)) {
            return 0;
        } else {
            long time = TimeUtil.getDateTimeFromGmt(eventTime);
            return time + repeat + (delay * MINUTE);
        }
    }

    public long generateTimerTime(String eventTime, long after) {
        if (TextUtils.isEmpty(eventTime)) {
            return 0;
        } else {
            long time = TimeUtil.getDateTimeFromGmt(eventTime);
            return time + after;
        }
    }

    public String getRemaining(String dateTime, int delay) {
        long time = TimeUtil.getDateTimeFromGmt(dateTime);
        return getRemaining(time + (delay * MINUTE));
    }

    public String getRemaining(long eventTime) {
        long difference = eventTime - System.currentTimeMillis();
        long days = (difference / (DAY));
        long hours = ((difference - (DAY * days)) / (HOUR));
        long minutes = (difference - (DAY * days) - (HOUR * hours)) / (MINUTE);
        hours = (hours < 0 ? -hours : hours);
        StringBuilder result = new StringBuilder();
        String lang = Locale.getDefault().toString().toLowerCase();
        if (difference > DAY) {
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                long last = days;
                while (last > 10) {
                    last -= 10;
                }
                if (last == 1 && days != 11) {
                    result.append(String.format(mContext.getString(R.string.x_day), String.valueOf(days)));
                } else if (last < 5 && (days < 12 || days > 14)) {
                    result.append(String.format(mContext.getString(R.string.x_dayzz), String.valueOf(days)));
                } else {
                    result.append(String.format(mContext.getString(R.string.x_days), String.valueOf(days)));
                }
            } else {
                if (days < 2)
                    result.append(String.format(mContext.getString(R.string.x_day), String.valueOf(days)));
                else
                    result.append(String.format(mContext.getString(R.string.x_days), String.valueOf(days)));
            }
        } else if (difference > HOUR) {
            hours = (days * 24) + hours;
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                long last = hours;
                while (last > 10) {
                    last -= 10;
                }
                if (last == 1 && hours != 11) {
                    result.append(String.format(mContext.getString(R.string.x_hour), String.valueOf(hours)));
                } else if (last < 5 && (hours < 12 || hours > 14)) {
                    result.append(String.format(mContext.getString(R.string.x_hourzz), String.valueOf(hours)));
                } else {
                    result.append(String.format(mContext.getString(R.string.x_hours), String.valueOf(hours)));
                }
            } else {
                if (hours < 2)
                    result.append(String.format(mContext.getString(R.string.x_hour), String.valueOf(hours)));
                else
                    result.append(String.format(mContext.getString(R.string.x_hours), String.valueOf(hours)));
            }
        } else if (difference > MINUTE) {
            minutes = (hours * 60) + minutes;
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                long last = minutes;
                while (last > 10) {
                    last -= 10;
                }
                if (last == 1 && minutes != 11) {
                    result.append(String.format(mContext.getString(R.string.x_minute), String.valueOf(minutes)));
                } else if (last < 5 && (minutes < 12 || minutes > 14)) {
                    result.append(String.format(mContext.getString(R.string.x_minutezz), String.valueOf(minutes)));
                } else {
                    result.append(String.format(mContext.getString(R.string.x_minutes), String.valueOf(minutes)));
                }
            } else {
                if (hours < 2)
                    result.append(String.format(mContext.getString(R.string.x_minute), String.valueOf(minutes)));
                else
                    result.append(String.format(mContext.getString(R.string.x_minutes), String.valueOf(minutes)));
            }
        } else if (difference > 0) {
            result.append(mContext.getString(R.string.less_than_minute));
        } else {
            result.append(mContext.getString(R.string.overdue));
        }
        return result.toString();
    }

    public boolean isNext(long due) {
        if (due == 0) return true;
        else {
            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(System.currentTimeMillis());
            long currentTome = cc.getTimeInMillis();
            return due > currentTome;
        }
    }

    public long getNextWeekdayTime(long startTime, List<Integer> weekdays, long delay) {
        if (weekdays == null) return 0;
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(startTime);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        if (delay > 0) {
            return startTime + (delay * MINUTE);
        } else {
            while (true) {
                int mDay = cc.get(Calendar.DAY_OF_WEEK);
                if (weekdays.get(mDay - 1) == 1) {
                    if (cc.getTimeInMillis() > System.currentTimeMillis()) {
                        break;
                    }
                }
                cc.setTimeInMillis(cc.getTimeInMillis() + DAY);
            }
            return cc.getTimeInMillis();
        }
    }

    public static boolean isCurrent(String eventTime) {
        return TimeUtil.getDateTimeFromGmt(eventTime) > System.currentTimeMillis();
    }

    public long getNextMonthDayTime(int dayOfMonth, long fromTime) {
        if (dayOfMonth == 0) {
            return getLastMonthDayTime(fromTime);
        }
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(fromTime);
        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if (cc.getTimeInMillis() > System.currentTimeMillis()) {
            return cc.getTimeInMillis();
        }
        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth + 1);
        while (cc.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
            cc.setTimeInMillis(cc.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        }
        return cc.getTimeInMillis();
    }

    public long getLastMonthDayTime(long fromTime) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(fromTime);
        while (true) {
            int lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH);
            Log.d(TAG, "getLastMonthDayTime: " + lastDay + ", m " + cc.get(Calendar.MONTH));
            cc.set(Calendar.DAY_OF_MONTH, lastDay);
            if (cc.getTimeInMillis() > System.currentTimeMillis()) {
                break;
            }
            cc.set(Calendar.DAY_OF_MONTH, 1);
            cc.add(Calendar.MONTH, 1);
        }
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        return cc.getTimeInMillis();
    }

    public static boolean isWeeekDay(List<Integer> days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return days.get(day) == 1;
    }

    public static boolean isDayOfMonth(int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return dayOfMonth == day;
    }
}
