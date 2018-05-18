package com.elementary.tasks.core.app_widgets.calendar;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

import com.elementary.tasks.R;
import com.elementary.tasks.core.app_widgets.WidgetUtils;
import com.elementary.tasks.core.app_widgets.voice_control.VoiceWidgetDialog;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.navigation.MainActivity;

import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;

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

public class CalendarWidget extends AppWidgetProvider {

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences sp = context.getSharedPreferences(
                CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Bundle options = appWidgetManager.getAppWidgetOptions(i);
                onAppWidgetOptionsChanged(context, appWidgetManager, i,
                        options);
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID) {
        Calendar cal = new GregorianCalendar();
        int month = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetID, 0);
        int year = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetID, 0);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        StringBuilder monthYearStringBuilder = new StringBuilder(50);
        Formatter monthYearFormatter = new Formatter(
                monthYearStringBuilder, Locale.getDefault());
        int monthYearFlag = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;
        String date = DateUtils.formatDateRange(context,
                monthYearFormatter, cal.getTimeInMillis(), cal.getTimeInMillis(), monthYearFlag)
                .toString().toUpperCase();

        int theme = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + widgetID, 0);
        CalendarTheme item = CalendarTheme.getThemes(context).get(theme);

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.calendar_widget_layout);
        rv.setTextViewText(R.id.currentDate, date);
        rv.setTextColor(R.id.currentDate, item.getTitleColor());

        rv.setInt(R.id.weekdayGrid, "setBackgroundResource", item.getWidgetBgColor());
        rv.setInt(R.id.header, "setBackgroundResource", item.getHeaderColor());
        rv.setInt(R.id.monthGrid, "setBackgroundResource", item.getBorderColor());

        WidgetUtils.setIcon(context, rv, item.getIconPlus(), R.id.plusButton);
        WidgetUtils.setIcon(context, rv, item.getIconVoice(), R.id.voiceButton);
        WidgetUtils.setIcon(context, rv, item.getIconSettings(), R.id.settingsButton);
        WidgetUtils.setIcon(context, rv, item.getRightArrow(), R.id.nextMonth);
        WidgetUtils.setIcon(context, rv, item.getLeftArrow(), R.id.prevMonth);

        Intent weekdayAdapter = new Intent(context, CalendarWeekdayService.class);
        weekdayAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        rv.setRemoteAdapter(R.id.weekdayGrid, weekdayAdapter);

        Intent startActivityIntent = new Intent(context, MainActivity.class);
        startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_calendar);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.monthGrid, startActivityPendingIntent);

        Intent monthAdapter = new Intent(context, CalendarMonthService.class);
        monthAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        rv.setRemoteAdapter(R.id.monthGrid, monthAdapter);

        Intent configIntent = new Intent(context, CreateReminderActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.plusButton, configPendingIntent);

        configIntent = new Intent(context, VoiceWidgetDialog.class);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.voiceButton, configPendingIntent);

        configIntent = new Intent(context, CalendarWidgetConfig.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent);

        Intent serviceIntent = new Intent(context, CalendarUpdateService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        serviceIntent.putExtra("actionPlus", 2);
        PendingIntent servicePendingIntent =
                PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.nextMonth, servicePendingIntent);

        serviceIntent = new Intent(context, CalendarUpdateMinusService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        serviceIntent.putExtra("actionMinus", 1);
        servicePendingIntent =
                PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.prevMonth, servicePendingIntent);

        appWidgetManager.updateAppWidget(widgetID, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.weekdayGrid);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.monthGrid);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences.Editor editor = context.getSharedPreferences(
                CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + widgetID);
            editor.remove(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetID);
            editor.remove(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetID);
        }
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context ctxt, AppWidgetManager mgr, int appWidgetId, Bundle newOptions) {
        RemoteViews updateViews = new RemoteViews(ctxt.getPackageName(), R.layout.calendar_widget_layout);
        mgr.updateAppWidget(appWidgetId, updateViews);
    }
}
