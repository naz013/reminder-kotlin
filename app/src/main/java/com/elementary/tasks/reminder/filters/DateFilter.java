package com.elementary.tasks.reminder.filters;

import android.app.AlarmManager;
import androidx.annotation.Nullable;

import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.Calendar;

/**
 * Copyright 2017 Nazar Suhovich
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
public class DateFilter extends AbstractFilter<DateFilter.DateRange, Reminder> {

    private DateRange range = DateRange.ALL;

    DateFilter(@Nullable ObjectFilter<Reminder> filter) {
        super(filter);
    }

    @Override
    public boolean filter(Reminder reminder) {
        if (!super.filter(reminder)) return false;
        if (range == DateRange.ALL) return true;
        else {
            switch (range) {
                case PERMANENT:
                    return reminder.getEventTime() == null;
                case TODAY:
                    return compareToday(reminder.getEventTime());
                case TOMORROW:
                    return compareTomorrow(reminder.getEventTime());
                default:
                    return reminder.getEventTime() == null;
            }
        }
    }

    private boolean compareTomorrow(String time) {
        return compareDay(time, 1);
    }

    private boolean compareToday(String time) {
        return compareDay(time, 0);
    }

    private boolean compareDay(@Nullable String time, int daysAfter) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + daysAfter * AlarmManager.INTERVAL_DAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        String start = TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        String end = TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis());
        if (time == null) return false;
        int st = time.compareTo(start);
        int ed = time.compareTo(end);
        return st >= 0 && ed <= 0;
    }

    @Override
    public void accept(DateRange value) throws Exception {
        this.range = value;
    }

    public enum DateRange {
        ALL,
        PERMANENT,
        TODAY,
        TOMORROW
    }
}
