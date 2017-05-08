package com.elementary.tasks.core.calendar;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

public final class FlextHelper {

    private static SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.ENGLISH);

    private FlextHelper(){
    }

    public static int getColor(Context context, @ColorRes int res){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(res, null);
        } else {
            return context.getResources().getColor(res);
        }
    }

    public static List<DateTime> getFullWeeks(int month, int year, int startDayOfWeek) {
        ArrayList<DateTime> datetimeList = new ArrayList<>();

        DateTime firstDateOfMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
        DateTime lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth
                .getNumDaysInMonth() - 1);
        int weekdayOfFirstDate = firstDateOfMonth.getWeekDay();
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7;
        }
        while (weekdayOfFirstDate > 0) {
            DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate
                    - startDayOfWeek);
            if (!dateTime.lt(firstDateOfMonth)) {
                break;
            }
            datetimeList.add(dateTime);
            weekdayOfFirstDate--;
        }
        for (int i = 0; i < lastDateOfMonth.getDay(); i++) {
            datetimeList.add(firstDateOfMonth.plusDays(i));
        }
        int endDayOfWeek = startDayOfWeek - 1;
        if (endDayOfWeek == 0) {
            endDayOfWeek = 7;
        }
        if (lastDateOfMonth.getWeekDay() != endDayOfWeek) {
            int i = 1;
            while (true) {
                DateTime nextDay = lastDateOfMonth.plusDays(i);
                datetimeList.add(nextDay);
                i++;
                if (nextDay.getWeekDay() == endDayOfWeek) {
                    break;
                }
            }
        }
        int size = datetimeList.size();
        int row = size / 7;
        int numOfDays = (6 - row) * 7;
        DateTime lastDateTime = datetimeList.get(size - 1);
        for (int i = 1; i <= numOfDays; i++) {
            DateTime nextDateTime = lastDateTime.plusDays(i);
            datetimeList.add(nextDateTime);
        }
        return datetimeList;
    }

    public static DateTime convertDateToDateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int javaMonth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        return new DateTime(year, javaMonth + 1, day, 0, 0, 0, 0);
    }

    public static Date convertDateTimeToDate(DateTime dateTime) {
        int year = dateTime.getYear();
        int datetimeMonth = dateTime.getMonth();
        int day = dateTime.getDay();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, datetimeMonth - 1, day);
        return calendar.getTime();
    }

    public static Date getDateFromString(String dateString, String dateFormat)
            throws ParseException {
        SimpleDateFormat formatter;
        if (dateFormat == null) {
            formatter = yyyyMMddFormat;
        } else {
            formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        }
        return formatter.parse(dateString);
    }

    public static DateTime getDateTimeFromString(String dateString, String dateFormat) {
        Date date;
        try {
            date = getDateFromString(dateString, dateFormat);
            return convertDateToDateTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DateTime convertToDateTime(long eventTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(eventTime);
        int year = calendar.get(Calendar.YEAR);
        int javaMonth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        try {
            return new DateTime(year, javaMonth + 1, day, 0, 0, 0, 0);
        } catch (Exception e) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            year = calendar.get(Calendar.YEAR);
            return new DateTime(year, javaMonth + 1, day, 0, 0, 0, 0);
        }
    }
}
