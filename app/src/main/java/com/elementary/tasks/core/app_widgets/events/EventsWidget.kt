package com.elementary.tasks.core.app_widgets.events

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews

import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.app_widgets.voice_control.VoiceWidgetDialog
import com.elementary.tasks.reminder.create_edit.AddReminderActivity

import java.text.SimpleDateFormat
import java.util.Calendar
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

class EventsWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val sp = context.getSharedPreferences(
                EventsWidgetConfig.EVENTS_WIDGET_PREF, Context.MODE_PRIVATE)

        for (i in appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val editor = context.getSharedPreferences(
                EventsWidgetConfig.EVENTS_WIDGET_PREF, Context.MODE_PRIVATE).edit()
        for (widgetID in appWidgetIds) {
            editor.remove(EventsWidgetConfig.EVENTS_WIDGET_THEME + widgetID)
            editor.remove(EventsWidgetConfig.EVENTS_WIDGET_TEXT_SIZE + widgetID)
        }
        editor.apply()
    }

    companion object {

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                         sp: SharedPreferences, widgetID: Int) {
            val cal = GregorianCalendar()
            val dateFormat = SimpleDateFormat("EEE, dd MMMM yyyy", Locale.getDefault())
            dateFormat.calendar = cal
            val date = dateFormat.format(cal.time)

            val rv = RemoteViews(context.packageName, R.layout.widget_current_tasks)
            rv.setTextViewText(R.id.widgetDate, date)
            val theme = sp.getInt(EventsWidgetConfig.EVENTS_WIDGET_THEME + widgetID, 0)
            val eventsTheme = EventsTheme.getThemes(context)[theme]
            val headerColor = eventsTheme.headerColor
            val backgroundColor = eventsTheme.backgroundColor
            val titleColor = eventsTheme.titleColor
            val plusIcon = eventsTheme.plusIcon
            val voiceIcon = eventsTheme.voiceIcon
            val settingsIcon = eventsTheme.settingsIcon

            rv.setTextColor(R.id.widgetDate, titleColor)
            rv.setInt(R.id.headerBg, "setBackgroundResource", headerColor)
            rv.setInt(R.id.widgetBg, "setBackgroundResource", backgroundColor)

            WidgetUtils.setIcon(context, rv, plusIcon, R.id.tasksCount)
            WidgetUtils.setIcon(context, rv, voiceIcon, R.id.voiceButton)
            WidgetUtils.setIcon(context, rv, settingsIcon, R.id.settingsButton)

            var configIntent = Intent(context, AddReminderActivity::class.java)
            var configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.tasksCount, configPendingIntent)

            configIntent = Intent(context, VoiceWidgetDialog::class.java)
            configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.voiceButton, configPendingIntent)

            configIntent = Intent(context, EventsWidgetConfig::class.java)
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent)

            val startActivityIntent = Intent(context, EventEditService::class.java)
            val startActivityPendingIntent = PendingIntent.getService(context, 0, startActivityIntent, 0)
            rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

            val adapter = Intent(context, EventsService::class.java)
            adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            rv.setRemoteAdapter(android.R.id.list, adapter)
            appWidgetManager.updateAppWidget(widgetID, rv)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, android.R.id.list)
        }
    }
}
