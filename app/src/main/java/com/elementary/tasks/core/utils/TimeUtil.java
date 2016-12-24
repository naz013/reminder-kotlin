package com.elementary.tasks.core.utils;

import android.app.AlarmManager;
import android.content.Context;

import com.elementary.tasks.R;

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

public class TimeUtil {

    public static final String GMT = "GMT";

    public static final SimpleDateFormat format24 = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
    public static final SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.getDefault());
    public static final SimpleDateFormat format12 = new SimpleDateFormat("dd MMM yyyy, K:mm a", Locale.getDefault());
    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat fullDateTime24 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault());
    public static final SimpleDateFormat fullDateTime12 = new SimpleDateFormat("EEE, dd MMM yyyy K:mm a", Locale.getDefault());
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat time24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final SimpleDateFormat time12 = new SimpleDateFormat("K:mm a", Locale.getDefault());
    public static final SimpleDateFormat simpleDate = new SimpleDateFormat("d MMMM", Locale.getDefault());
    public static final SimpleDateFormat simpleDateTime = new SimpleDateFormat("d MMMM, HH:mm", Locale.getDefault());
    public static final SimpleDateFormat simpleDateTime12 = new SimpleDateFormat("d MMMM, K:mm a", Locale.getDefault());

    public static SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.getDefault());

    public TimeUtil(){}

    public static String getBirthdayTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return time24.format(calendar.getTime());
    }

    public static long getBirthdayTime(String time) {
        Calendar calendar = Calendar.getInstance();
        if (time != null) {
            try {
                Date date = time24.parse(time);
                calendar.setTime(date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return calendar.getTimeInMillis();
    }

    public static Calendar getBirthdayCalendar(String time) {
        Calendar calendar = Calendar.getInstance();
        if (time != null) {
            try {
                Date date = time24.parse(time);
                calendar.setTime(date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return calendar;
    }

    public static String getGmtDateTime() {
        gmtDateFormat.setTimeZone(TimeZone.getTimeZone(GMT));
        return gmtDateFormat.format(new Date());
    }

    public static String getGmtFromDateTime(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        gmtDateFormat.setTimeZone(TimeZone.getTimeZone(GMT));
        return gmtDateFormat.format(calendar.getTime());
    }

    public static long getDateTimeFromGmt(String dateTime){
        if (dateTime == null) return 0;
        Calendar calendar = Calendar.getInstance();
        try {
            gmtDateFormat.setTimeZone(TimeZone.getTimeZone(GMT));
            Date date = gmtDateFormat.parse(dateTime);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar.getTimeInMillis();
    }

    public static String getFullDateTime(long date, boolean is24){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        if (is24) return fullDateTime24.format(calendar.getTime());
        else return fullDateTime12.format(calendar.getTime());
    }

    public static String getRealDateTime(String gmt, int delay, boolean is24) {
        Calendar calendar = Calendar.getInstance();
        try {
            gmtDateFormat.setTimeZone(TimeZone.getTimeZone(GMT));
            Date date = gmtDateFormat.parse(gmt);
            calendar.setTime(date);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + (delay * TimeCount.MINUTE));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (is24) return fullDateTime24.format(calendar.getTime());
        else return fullDateTime12.format(calendar.getTime());
    }

    public static String getDateTimeFromGmt(String dateTime, boolean is24){
        Calendar calendar = Calendar.getInstance();
        try {
            gmtDateFormat.setTimeZone(TimeZone.getTimeZone(GMT));
            Date date = gmtDateFormat.parse(dateTime);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (is24) return fullDateTime24.format(calendar.getTime());
        else return fullDateTime12.format(calendar.getTime());
    }

    public static String getSimpleDate(String gmtDate) {
        return getSimpleDate(getDateTimeFromGmt(gmtDate));
    }

    public static String getSimpleDate(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return simpleDate.format(calendar.getTime());
    }

    public static String getDate(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return dateFormat.format(calendar.getTime());
    }

    public static String getSimpleDateTime(long date, boolean is24){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        if (is24) return simpleDateTime.format(calendar.getTime());
        else return simpleDateTime12.format(calendar.getTime());
    }

    public static String getDate(Date date){
        return fullDateFormat.format(date);
    }

    public static String getTimeStamp(){
        return timeStampFormat.format(new Date());
    }

    public static Date getDate(String date){
        try {
            return time24.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getAge(String dateOfBirth){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(dateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (date != null) calendar.setTime(date);
        int yearOfBirth = calendar.get(Calendar.YEAR);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.getTimeInMillis();
        int currentYear = calendar1.get(Calendar.YEAR);
        return currentYear - yearOfBirth;
    }

    public static String getDateTime(Date date, boolean is24){
        if (is24) return format24.format(date);
        else return format12.format(date);
    }

    public static String getTime(Date date, boolean is24){
        if (is24) return time24.format(date);
        else return time12.format(date);
    }

    public static int getAge(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int mYear = calendar.get(Calendar.YEAR);
        return mYear - year;
    }

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

    public static String generateAfterString(long time){
        long s = 1000;
        long m = s * 60;
        long h = m * 60;
        long hours = (time / h);
        long minutes = ((time - hours * h) / (m));
        long seconds = ((time - (hours * h) - (minutes * m)) / (s));
        String hourStr;
        if (hours < 10) hourStr = "0" + hours;
        else hourStr = String.valueOf(hours);
        String minuteStr;
        if (minutes < 10) minuteStr = "0" + minutes;
        else minuteStr = String.valueOf(minutes);
        String secondStr;
        if (seconds < 10) secondStr = "0" + seconds;
        else secondStr = String.valueOf(seconds);
        return hourStr + minuteStr + secondStr;
    }

    public static String getAgeFormatted(Context mContext, String date) {
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
}
