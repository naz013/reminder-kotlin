package com.elementary.tasks.core.utils;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.work.CheckBirthdaysAsync;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

public final class TimeUtil {

    public static final String GMT = "GMT";

    public static final SimpleDateFormat FORMAT_24 = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
    public static final SimpleDateFormat TIME_STAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.getDefault());
    public static final SimpleDateFormat FORMAT_12 = new SimpleDateFormat("dd MMM yyyy, K:mm a", Locale.getDefault());
    public static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat FULL_DATE_TIME_24 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault());
    public static final SimpleDateFormat FULL_DATE_TIME_12 = new SimpleDateFormat("EEE, dd MMM yyyy K:mm a", Locale.getDefault());
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat TIME_24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final SimpleDateFormat TIME_12 = new SimpleDateFormat("K:mm a", Locale.getDefault());
    public static final SimpleDateFormat SIMPLE_DATE = new SimpleDateFormat("d MMMM", Locale.getDefault());
    public static final SimpleDateFormat SIMPLE_DATE_TIME = new SimpleDateFormat("d MMMM, HH:mm", Locale.getDefault());
    public static final SimpleDateFormat SIMPLE_DATE_TIME_12 = new SimpleDateFormat("d MMMM, K:mm a", Locale.getDefault());

    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.getDefault());
    private static final SimpleDateFormat FIRE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private TimeUtil() {
    }

    @Nullable
    public static String getFireFormatted(@NonNull Context context, @Nullable String gmt) {
        if (TextUtils.isEmpty(gmt)) return null;
        try {
            FIRE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(GMT));
            Date date = FIRE_DATE_FORMAT.parse(gmt);
            if (Prefs.getInstance(context).is24HourFormatEnabled()) {
                return FORMAT_24.format(date);
            } else {
                return FORMAT_12.format(date);
            }
        } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TimePickerDialog showTimePicker(Context context, TimePickerDialog.OnTimeSetListener listener,
                                                  int hour, int minute) {
        boolean is24 = Prefs.getInstance(context).is24HourFormatEnabled();
        TimePickerDialog dialog = new TimePickerDialog(context, listener, hour, minute, is24);
        dialog.show();
        return dialog;
    }

    public static DatePickerDialog showDatePicker(Context context, DatePickerDialog.OnDateSetListener listener,
                                                  int year, int month, int dayOfMonth) {
        DatePickerDialog dialog = new DatePickerDialog(context, listener, year, month, dayOfMonth);
        if (Module.isLollipop()) {
            dialog.getDatePicker().setFirstDayOfWeek(Prefs.getInstance(context).getStartDay() + 1);
        }
        dialog.show();
        return dialog;
    }

    @Nullable
    public static DateItem getFutureBirthdayDate(Context context, String fullDate) {
        Date date = null;
        String dateTime = Prefs.getInstance(context).getBirthdayTime();
        try {
            date = CheckBirthdaysAsync.DATE_FORMAT.parse(fullDate);
        } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int bDay = calendar.get(Calendar.DAY_OF_MONTH);
            int bMonth = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            calendar.setTimeInMillis(TimeUtil.getBirthdayTime(dateTime));
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.MONTH, bMonth);
            calendar.set(Calendar.DAY_OF_MONTH, bDay);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.YEAR, 1);
            }
            return new DateItem(calendar, year);
        }
        return null;
    }

    @NonNull
    public static String getBirthdayTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return TIME_24.format(calendar.getTime());
    }

    public static long getBirthdayTime(@Nullable String time) {
        Calendar calendar = Calendar.getInstance();
        if (time != null) {
            try {
                Date date = TIME_24.parse(time);
                calendar.setTime(date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                }
            } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return calendar.getTimeInMillis();
    }

    @NonNull
    public static Calendar getBirthdayCalendar(@Nullable String time) {
        Calendar calendar = Calendar.getInstance();
        if (time != null) {
            try {
                Date date = TIME_24.parse(time);
                calendar.setTime(date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                }
            } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return calendar;
    }

    @NonNull
    public static String getGmtDateTime() {
        GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(GMT));
        return GMT_DATE_FORMAT.format(new Date());
    }

    @NonNull
    public static String getGmtFromDateTime(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(GMT));
        return GMT_DATE_FORMAT.format(calendar.getTime());
    }

    public static long getDateTimeFromGmt(@Nullable String dateTime) {
        if (TextUtils.isEmpty(dateTime)) {
            return 0;
        }
        Calendar calendar = Calendar.getInstance();
        try {
            GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(GMT));
            Date date = GMT_DATE_FORMAT.parse(dateTime);
            calendar.setTime(date);
        } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return calendar.getTimeInMillis();
    }

    @NonNull
    public static String getFullDateTime(long date, boolean is24, boolean isLog) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        if (isLog) {
            return TIME_STAMP_FORMAT.format(calendar.getTime());
        } else {
            if (is24) {
                return FULL_DATE_TIME_24.format(calendar.getTime());
            } else {
                return FULL_DATE_TIME_12.format(calendar.getTime());
            }
        }
    }

    @Nullable
    public static String getVoiceDateTime(@Nullable String date, boolean is24, int locale) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getDateTimeFromGmt(date));
        Locale loc = new Locale(Language.getTextLanguage(locale));
        DateFormat format = new SimpleDateFormat("EEEE, MMMM dd yyyy K:mm a", loc);
        if (locale == 0) {
            if (is24) {
                format = new SimpleDateFormat("EEEE, MMMM dd yyyy HH:mm", loc);
            }
        } else {
            if (is24) {
                format = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc);
            } else {
                format = new SimpleDateFormat("EEEE, dd MMMM yyyy K:mm a", loc);
            }
        }
        return format.format(calendar.getTime());
    }

    @NonNull
    public static String getFullDateTime(@Nullable String date) {
        if (TextUtils.isEmpty(date)) {
            return "No event time";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getDateTimeFromGmt(date));
        return TIME_STAMP_FORMAT.format(calendar.getTime());
    }

    @NonNull
    public static String getRealDateTime(@Nullable String gmt, int delay, boolean is24) {
        if (TextUtils.isEmpty(gmt)) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        try {
            GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(GMT));
            Date date = GMT_DATE_FORMAT.parse(gmt);
            calendar.setTime(date);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + (delay * TimeCount.MINUTE));
        } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        if (is24) {
            return FULL_DATE_TIME_24.format(calendar.getTime());
        } else {
            return FULL_DATE_TIME_12.format(calendar.getTime());
        }
    }

    @NonNull
    public static String getDateTimeFromGmt(@Nullable String dateTime, boolean is24) {
        Calendar calendar = Calendar.getInstance();
        try {
            GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(GMT));
            Date date = GMT_DATE_FORMAT.parse(dateTime);
            calendar.setTime(date);
        } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        if (is24) {
            return FULL_DATE_TIME_24.format(calendar.getTime());
        } else {
            return FULL_DATE_TIME_12.format(calendar.getTime());
        }
    }

    @NonNull
    public static String getSimpleDate(@Nullable String gmtDate) {
        return getSimpleDate(getDateTimeFromGmt(gmtDate));
    }

    @NonNull
    public static String getSimpleDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return SIMPLE_DATE.format(calendar.getTime());
    }

    @NonNull
    public static String getDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return DATE_FORMAT.format(calendar.getTime());
    }

    public static String getSimpleDateTime(long date, boolean is24) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        if (is24) {
            return SIMPLE_DATE_TIME.format(calendar.getTime());
        } else {
            return SIMPLE_DATE_TIME_12.format(calendar.getTime());
        }
    }

    @NonNull
    public static String getDate(@NonNull Date date) {
        return FULL_DATE_FORMAT.format(date);
    }

    @NonNull
    public static String getDate(@NonNull Date date, @NonNull DateFormat format) {
        format.setTimeZone(TimeZone.getDefault());
        return format.format(date);
    }

    @Nullable
    public static Date getDate(@Nullable String date) {
        try {
            return TIME_24.parse(date);
        } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static int getAge(@Nullable String dateOfBirth) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(dateOfBirth);
        } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (date != null) {
            calendar.setTime(date);
        }
        int yearOfBirth = calendar.get(Calendar.YEAR);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.getTimeInMillis();
        int currentYear = calendar1.get(Calendar.YEAR);
        return currentYear - yearOfBirth;
    }

    @NonNull
    public static String getDateTime(@NonNull Date date, boolean is24) {
        if (is24) {
            return FORMAT_24.format(date);
        } else {
            return FORMAT_12.format(date);
        }
    }

    @NonNull
    public static String getTime(@NonNull Date date, boolean is24) {
        if (is24) {
            return TIME_24.format(date);
        } else {
            return TIME_12.format(date);
        }
    }

    public static int getAge(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int mYear = calendar.get(Calendar.YEAR);
        return mYear - year;
    }

    @NonNull
    public static Date getDate(int year, int month, int day) {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.YEAR, year);
        cal1.set(Calendar.MONTH, month);
        cal1.set(Calendar.DAY_OF_MONTH, day);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        return cal1.getTime();
    }

    @NonNull
    public static String generateAfterString(long time) {
        long s = 1000;
        long m = s * 60;
        long h = m * 60;
        long hours = (time / h);
        long minutes = ((time - hours * h) / (m));
        long seconds = ((time - (hours * h) - (minutes * m)) / (s));
        String hourStr;
        if (hours < 10) {
            hourStr = "0" + hours;
        } else {
            hourStr = String.valueOf(hours);
        }
        String minuteStr;
        if (minutes < 10) {
            minuteStr = "0" + minutes;
        } else {
            minuteStr = String.valueOf(minutes);
        }
        String secondStr;
        if (seconds < 10) {
            secondStr = "0" + seconds;
        } else {
            secondStr = String.valueOf(seconds);
        }
        return hourStr + minuteStr + secondStr;
    }

    @NonNull
    public static String getAgeFormatted(Context mContext, @Nullable String date) {
        int years = getAge(date);
        StringBuilder result = new StringBuilder();
        String lang = Locale.getDefault().getLanguage().toLowerCase();
        if (lang.startsWith("uk") || lang.startsWith("ru")) {
            long last = years;
            while (last > 10) {
                last -= 10;
            }
            if (last == 1 && years != 11) {
                result.append(String.format(mContext.getString(R.string.x_year), String.valueOf(years)));
            } else if (last < 5 && (years < 12 || years > 14)) {
                result.append(String.format(mContext.getString(R.string.x_yearzz), String.valueOf(years)));
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), String.valueOf(years)));
            }
        } else {
            if (years < 2) {
                result.append(String.format(mContext.getString(R.string.x_year), String.valueOf(years)));
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), String.valueOf(years)));
            }
        }
        return result.toString();
    }

    @NonNull
    public static String getAgeFormatted(Context mContext, int date) {
        int years = getAge(date);
        StringBuilder result = new StringBuilder();
        String lang = Locale.getDefault().toString().toLowerCase();
        if (lang.startsWith("uk") || lang.startsWith("ru")) {
            long last = years;
            while (last > 10) {
                last -= 10;
            }
            if (last == 1 && years != 11) {
                result.append(String.format(mContext.getString(R.string.x_year), String.valueOf(years)));
            } else if (last < 5 && (years < 12 || years > 14)) {
                result.append(String.format(mContext.getString(R.string.x_yearzz), String.valueOf(years)));
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), String.valueOf(years)));
            }
        } else {
            if (years < 2) {
                result.append(String.format(mContext.getString(R.string.x_year), String.valueOf(years)));
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), String.valueOf(years)));
            }
        }
        return result.toString();
    }

    public static class DateItem {
        final Calendar calendar;
        final int year;

        public DateItem(Calendar calendar, int year) {
            this.calendar = calendar;
            this.year = year;
        }

        public Calendar getCalendar() {
            return calendar;
        }

        public int getYear() {
            return year;
        }
    }
}
