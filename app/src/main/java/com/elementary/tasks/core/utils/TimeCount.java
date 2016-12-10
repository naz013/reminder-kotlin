package com.elementary.tasks.core.utils;

import android.app.AlarmManager;
import android.content.Context;
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

    public boolean isRange(List<Integer> hours, String fromHour, String toHour) {
        if (hours == null && fromHour == null && toHour == null) return true;
        boolean res = false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hours != null) {
            return hours.contains(mHour);
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
                if (start > end) {
                    res = eventTime >= start || eventTime < end;
                } else {
                    res = eventTime >= start && eventTime <= end;
                }
            }
        }
        return res;
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

    public long generateStartEvent(int type, long time, List<Integer> weekdays, long after) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (Reminder.isBase(type, Reminder.BY_WEEK)) {
            return getNextWeekdayTime(calendar.getTimeInMillis(), weekdays, 0);
        } else if (Reminder.isBase(type, Reminder.BY_MONTH)) {
            return getNextMonthDayTime(calendar.get(Calendar.DAY_OF_MONTH), calendar.getTimeInMillis());
        } else if (Reminder.isSame(type, Reminder.BY_TIME)) {
            return System.currentTimeMillis() + after;
        } else {
            if (time == 0) return 0;
            return calendar.getTimeInMillis();
        }
    }

    public long generateDateTime(int type, int dayOfMonth, long startTime, long repeat,
                                 List<Integer> weekdays, long count, long delay) {
        long dateTime;
        if (startTime == 0) {
            dateTime = 0;
        } else {
            if (Reminder.isBase(type, Reminder.BY_WEEK)) {
                dateTime = getNextWeekdayTime(startTime, weekdays, delay);
            } else if (Reminder.isBase(type, Reminder.BY_MONTH)) {
                dateTime = getNextMonthDayTime(dayOfMonth, startTime);
            } else {
                dateTime = startTime + (repeat * count) + (delay * MINUTE);
            }
        }
        return dateTime;
    }

    public String getRemaining(String dateTime) {
        long time = TimeUtil.getDateTimeFromGmt(dateTime);
        return getRemaining(time);
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

    public static long getNextWeekdayTime(long startTime, List<Integer> weekdays, long delay) {
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

    public boolean isCurrent(long startTime) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currentTome = cc.getTimeInMillis();
        return startTime < currentTome;
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

    public static long getLastMonthDayTime(long fromTime) {
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
