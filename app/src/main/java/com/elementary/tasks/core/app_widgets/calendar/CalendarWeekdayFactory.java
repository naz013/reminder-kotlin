package com.elementary.tasks.core.app_widgets.calendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.elementary.tasks.R;
import com.elementary.tasks.core.calendar.FlextHelper;
import com.elementary.tasks.core.utils.Prefs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

class CalendarWeekdayFactory implements RemoteViewsService.RemoteViewsFactory {

    @NonNull
    private List<String> mWeekdaysList = new ArrayList<>();
    private Context mContext;
    private int mWidgetId;
    private int SUNDAY = 1;
    private int startDayOfWeek = SUNDAY;

    CalendarWeekdayFactory(Context ctx, Intent intent) {
        mContext = ctx;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mWeekdaysList.clear();
    }

    @Override
    public void onDataSetChanged() {
        mWeekdaysList.clear();
        SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());

        DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
        DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);
        if (Prefs.getInstance(mContext).getStartDay() == 1) {
            nextDay = nextDay.plusDays(1);
        }
        for (int i = 0; i < 7; i++) {
            Date date = FlextHelper.convertDateTimeToDate(nextDay);
            mWeekdaysList.add(fmt.format(date).toUpperCase());
            nextDay = nextDay.plusDays(1);
        }
    }

    @Override
    public void onDestroy() {
        mWeekdaysList.clear();
    }

    @Override
    public int getCount() {
        return mWeekdaysList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        int theme = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + mWidgetId, 0);
        CalendarTheme item = CalendarTheme.getThemes(mContext).get(theme);
        int itemTextColor = item.getItemTextColor();
        RemoteViews rView = new RemoteViews(mContext.getPackageName(), R.layout.weekday_grid);
        rView.setTextViewText(R.id.textView1, mWeekdaysList.get(i));
        rView.setTextColor(R.id.textView1, itemTextColor);
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
}