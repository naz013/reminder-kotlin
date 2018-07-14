package com.elementary.tasks.core.appWidgets.calendar

import android.annotation.TargetApi
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.RemoteViews

import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.WidgetUtils
import com.elementary.tasks.core.appWidgets.voiceControl.VoiceWidgetDialog
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity
import com.elementary.tasks.navigation.MainActivity

import java.util.Calendar
import java.util.Formatter
import java.util.GregorianCalendar
import java.util.Locale

/**
 * Copyright 2015 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class CalendarWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val sp = context.getSharedPreferences(
                CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE)
        for (i in appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                val options = appWidgetManager.getAppWidgetOptions(i)
                onAppWidgetOptionsChanged(context, appWidgetManager, i,
                        options)
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val editor = context.getSharedPreferences(
                CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE).edit()
        for (widgetID in appWidgetIds) {
            editor.remove(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + widgetID)
            editor.remove(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetID)
            editor.remove(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetID)
        }
        editor.apply()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onAppWidgetOptionsChanged(ctxt: Context, mgr: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        val updateViews = RemoteViews(ctxt.packageName, R.layout.widget_calendar)
        mgr.updateAppWidget(appWidgetId, updateViews)
    }

    companion object {

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                         sp: SharedPreferences, widgetID: Int) {
            val cal = GregorianCalendar()
            val month = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetID, 0)
            val year = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetID, 0)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.YEAR, year)
            val monthYearStringBuilder = StringBuilder(50)
            val monthYearFormatter = Formatter(
                    monthYearStringBuilder, Locale.getDefault())
            val monthYearFlag = (DateUtils.FORMAT_SHOW_DATE
                    or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR)
            val date = DateUtils.formatDateRange(context,
                    monthYearFormatter, cal.timeInMillis, cal.timeInMillis, monthYearFlag)
                    .toString().toUpperCase()

            val theme = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + widgetID, 0)
            val item = CalendarTheme.getThemes(context)[theme]

            val rv = RemoteViews(context.packageName, R.layout.widget_calendar)
            rv.setTextViewText(R.id.currentDate, date)
            rv.setTextColor(R.id.currentDate, item.titleColor)

            rv.setInt(R.id.weekdayGrid, "setBackgroundResource", item.widgetBgColor)
            rv.setInt(R.id.header, "setBackgroundResource", item.headerColor)
            rv.setInt(R.id.monthGrid, "setBackgroundResource", item.borderColor)

            WidgetUtils.setIcon(context, rv, item.iconPlus, R.id.plusButton)
            WidgetUtils.setIcon(context, rv, item.iconVoice, R.id.voiceButton)
            WidgetUtils.setIcon(context, rv, item.iconSettings, R.id.settingsButton)
            WidgetUtils.setIcon(context, rv, item.rightArrow, R.id.nextMonth)
            WidgetUtils.setIcon(context, rv, item.leftArrow, R.id.prevMonth)

            val weekdayAdapter = Intent(context, CalendarWeekdayService::class.java)
            weekdayAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            rv.setRemoteAdapter(R.id.weekdayGrid, weekdayAdapter)

            val startActivityIntent = Intent(context, MainActivity::class.java)
            startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_calendar)
            val startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                    startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(R.id.monthGrid, startActivityPendingIntent)

            val monthAdapter = Intent(context, CalendarMonthService::class.java)
            monthAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            rv.setRemoteAdapter(R.id.monthGrid, monthAdapter)

            var configIntent = Intent(context, CreateReminderActivity::class.java)
            var configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.plusButton, configPendingIntent)

            configIntent = Intent(context, VoiceWidgetDialog::class.java)
            configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.voiceButton, configPendingIntent)

            configIntent = Intent(context, CalendarWidgetConfig::class.java)
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent)

            var serviceIntent = Intent(context, CalendarUpdateService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            serviceIntent.putExtra("actionPlus", 2)
            var servicePendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.nextMonth, servicePendingIntent)

            serviceIntent = Intent(context, CalendarUpdateMinusService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            serviceIntent.putExtra("actionMinus", 1)
            servicePendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.prevMonth, servicePendingIntent)

            appWidgetManager.updateAppWidget(widgetID, rv)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.weekdayGrid)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.monthGrid)
        }
    }
}
