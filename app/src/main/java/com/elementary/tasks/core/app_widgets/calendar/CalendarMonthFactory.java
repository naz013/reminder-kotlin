package com.elementary.tasks.core.app_widgets.calendar;

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.elementary.tasks.R;
import com.elementary.tasks.core.app_widgets.WidgetDataProvider;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hirondelle.date4j.DateTime;

/**
 * Copyright 2015 Nazar Suhovich
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

public class CalendarMonthFactory implements RemoteViewsService.RemoteViewsFactory {

    @NonNull
    private List<DateTime> mDateTimeList = new ArrayList<>();
    @NonNull
    private List<WidgetItem> mPagerData = new ArrayList<>();
    private Context mContext;
    private int mWidgetId;
    private int mDay;
    private int mMonth;
    private int mYear;

    CalendarMonthFactory(Context ctx, Intent intent) {
        mContext = ctx;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mDateTimeList.clear();
        mPagerData.clear();
    }

    @Override
    public void onDataSetChanged() {
        mDateTimeList.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        SharedPreferences sp =
                mContext.getSharedPreferences(CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        int prefsMonth = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + mWidgetId, 0);

        mYear = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + mWidgetId, calendar.get(Calendar.YEAR));
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = prefsMonth + 1;

        DateTime firstDateOfMonth = new DateTime(mYear, prefsMonth + 1, 1, 0, 0, 0, 0);
        DateTime lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.getNumDaysInMonth() - 1);

        int weekdayOfFirstDate = firstDateOfMonth.getWeekDay();
        int startDayOfWeek = Prefs.getInstance(mContext).getStartDay() + 1;

        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7;
        }

        while (weekdayOfFirstDate > 0) {
            DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - startDayOfWeek);
            if (!dateTime.lt(firstDateOfMonth)) {
                break;
            }
            mDateTimeList.add(dateTime);
            weekdayOfFirstDate--;
        }
        for (int i = 0; i < lastDateOfMonth.getDay(); i++) {
            mDateTimeList.add(firstDateOfMonth.plusDays(i));
        }
        int endDayOfWeek = startDayOfWeek - 1;
        if (endDayOfWeek == 0) {
            endDayOfWeek = 7;
        }
        if (lastDateOfMonth.getWeekDay() != endDayOfWeek) {
            int i = 1;
            while (true) {
                DateTime nextDay = lastDateOfMonth.plusDays(i);
                mDateTimeList.add(nextDay);
                i++;
                if (nextDay.getWeekDay() == endDayOfWeek) {
                    break;
                }
            }
        }
        int size = mDateTimeList.size();
        int numOfDays = 42 - size;
        DateTime lastDateTime = mDateTimeList.get(size - 1);
        for (int i = 1; i <= numOfDays; i++) {
            DateTime nextDateTime = lastDateTime.plusDays(i);
            mDateTimeList.add(nextDateTime);
        }
        showEvents();
    }

    private void showEvents() {
        Calendar calendar = Calendar.getInstance();
        Prefs sPrefs = Prefs.getInstance(mContext);
        calendar.setTimeInMillis(TimeUtil.getBirthdayTime(sPrefs.getBirthdayTime()));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        boolean isFeature = sPrefs.isFutureEventEnabled();
        boolean isRemindersEnabled = sPrefs.isRemindersInCalendarEnabled();

        WidgetDataProvider provider = new WidgetDataProvider(mContext);
        provider.setTime(hour, minute);
        if (isRemindersEnabled) {
            provider.setFeature(isFeature);
        }
        provider.fillArray();
        mPagerData.clear();

        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentDay;
        int currentMonth;
        int currentYear;
        int position = 0;
        do {
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            boolean hasReminders = provider.hasReminder(currentDay, currentMonth, currentYear);
            boolean hasBirthdays = provider.hasBirthday(currentDay, currentMonth);
            mPagerData.add(new WidgetItem(currentDay, currentMonth, currentYear,
                    hasReminders, hasBirthdays));
            position++;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        } while (position < Configs.MAX_DAYS_COUNT);
    }

    @Override
    public void onDestroy() {
        mDateTimeList.clear();
        mPagerData.clear();
    }

    @Override
    public int getCount() {
        return mDateTimeList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(
                CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        int theme = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + mWidgetId, 0);
        CalendarTheme calendarTheme = CalendarTheme.getThemes(mContext).get(theme);
        int itemTextColor = calendarTheme.getItemTextColor();
        int rowColor = calendarTheme.getRowColor();
        int reminderM = calendarTheme.getReminderMark();
        int birthdayM = calendarTheme.getBirthdayMark();
        int currentM = calendarTheme.getCurrentMark();
        int prefsMonth = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + mWidgetId, 0);
        RemoteViews rView = new RemoteViews(mContext.getPackageName(), R.layout.month_view_grid);

        ThemeUtil cs = ThemeUtil.getInstance(mContext);

        int selDay = mDateTimeList.get(i).getDay();
        int selMonth = mDateTimeList.get(i).getMonth();
        int selYear = mDateTimeList.get(i).getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int realMonth = calendar.get(Calendar.MONTH);
        int realYear = calendar.get(Calendar.YEAR);

        rView.setTextViewText(R.id.textView, String.valueOf(selDay));
        if (selMonth == prefsMonth + 1) {
            rView.setTextColor(R.id.textView, itemTextColor);
        } else {
            rView.setTextColor(R.id.textView, mContext.getResources().getColor(R.color.material_grey));
        }
        rView.setInt(R.id.background, "setBackgroundResource", rowColor);

        rView.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT);
        rView.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT);
        rView.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT);

        if (mPagerData.size() > 0) {
            for (WidgetItem item : mPagerData) {
                int day = item.getDay();
                int month = item.getMonth() + 1;
                int year = item.getYear();
                if (day == selDay && month == selMonth) {
                    if (item.isHasReminders() && year == selYear) {
                        if (reminderM != 0) {
                            rView.setInt(R.id.reminderMark, "setBackgroundResource", reminderM);
                        } else {
                            rView.setInt(R.id.reminderMark, "setBackgroundColor",
                                    mContext.getResources().getColor(cs.colorReminderCalendar()));
                        }
                    } else {
                        rView.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT);
                    }
                    if (item.isHasBirthdays()) {
                        if (birthdayM != 0) {
                            rView.setInt(R.id.birthdayMark, "setBackgroundResource", birthdayM);
                        } else {
                            rView.setInt(R.id.birthdayMark, "setBackgroundColor",
                                    mContext.getResources().getColor(cs.colorBirthdayCalendar()));
                        }
                    } else {
                        rView.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT);
                    }
                    break;
                }
            }
        }

        if (mDay == selDay && mMonth == selMonth && mYear == realYear && mMonth == realMonth + 1
                && mYear == selYear) {
            if (currentM != 0) {
                rView.setInt(R.id.currentMark, "setBackgroundResource", currentM);
            } else {
                rView.setInt(R.id.currentMark, "setBackgroundColor",
                        mContext.getResources().getColor(cs.colorCurrentCalendar()));
            }
        } else {
            rView.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT);
        }

        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MONTH, selMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, selDay);
        calendar.set(Calendar.YEAR, selYear);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        long dateMills = calendar.getTimeInMillis();

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("date", dateMills);
        rView.setOnClickFillInIntent(R.id.textView, fillInIntent);
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static class WidgetItem {
        private int day, month, year;
        private boolean hasReminders, hasBirthdays;

        WidgetItem(int day, int month, int year, boolean hasReminders, boolean hasBirthdays) {
            this.day = day;
            this.month = month;
            this.year = year;
            this.hasReminders = hasReminders;
            this.hasBirthdays = hasBirthdays;
        }

        boolean isHasBirthdays() {
            return hasBirthdays;
        }

        boolean isHasReminders() {
            return hasReminders;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }
    }
}