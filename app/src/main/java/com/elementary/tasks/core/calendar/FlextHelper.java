package com.elementary.tasks.core.calendar;

import android.content.Context;
import android.os.Build;
import androidx.annotation.ColorRes;

import java.util.Calendar;
import java.util.Date;

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

    private FlextHelper() {
    }

    public static int getColor(Context context, @ColorRes int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(res, null);
        } else {
            return context.getResources().getColor(res);
        }
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

    public static DateTime convertToDateTime(long eventTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(eventTime);
        int year = calendar.get(Calendar.YEAR);
        int javaMonth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        try {
            return new DateTime(year, javaMonth + 1, day, 0, 0, 0, 0);
        } catch (Exception e) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            year = calendar.get(Calendar.YEAR);
            try {
                return new DateTime(year, javaMonth + 1, day, 0, 0, 0, 0);
            } catch (Exception e1) {
                return new DateTime(year, javaMonth + 1, day - 1, 0, 0, 0, 0);
            }
        }
    }
}
