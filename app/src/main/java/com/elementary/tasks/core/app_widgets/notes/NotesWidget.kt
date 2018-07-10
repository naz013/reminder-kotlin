package com.elementary.tasks.core.app_widgets.notes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews

import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.notes.create.CreateNoteActivity

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

class NotesWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val sp = context.getSharedPreferences(
                NotesWidgetConfig.NOTES_WIDGET_PREF, Context.MODE_PRIVATE)
        for (i in appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val editor = context.getSharedPreferences(
                NotesWidgetConfig.NOTES_WIDGET_PREF, Context.MODE_PRIVATE).edit()
        for (widgetID in appWidgetIds) {
            editor.remove(NotesWidgetConfig.NOTES_WIDGET_THEME + widgetID)
        }
        editor.apply()
    }

    companion object {

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                         sp: SharedPreferences, widgetID: Int) {
            val rv = RemoteViews(context.packageName, R.layout.widget_note)
            val theme = sp.getInt(NotesWidgetConfig.NOTES_WIDGET_THEME + widgetID, 0)
            val notesTheme = NotesTheme.getThemes(context)[theme]

            val headerColor = notesTheme.headerColor
            val backgroundColor = notesTheme.backgroundColor
            val titleColor = notesTheme.titleColor
            val plusIcon = notesTheme.plusIcon
            val settingsIcon = notesTheme.settingsIcon

            rv.setInt(R.id.headerBg, "setBackgroundResource", headerColor)
            rv.setInt(R.id.widgetBg, "setBackgroundResource", backgroundColor)
            rv.setTextColor(R.id.widgetTitle, titleColor)
            WidgetUtils.setIcon(context, rv, plusIcon, R.id.tasksCount)

            var configIntent = Intent(context, CreateNoteActivity::class.java)
            var configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.tasksCount, configPendingIntent)

            configIntent = Intent(context, NotesWidgetConfig::class.java)
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent)
            WidgetUtils.setIcon(context, rv, settingsIcon, R.id.settingsButton)

            val startActivityIntent = Intent(context, CreateNoteActivity::class.java)
            val startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

            val adapter = Intent(context, NotesService::class.java)
            adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            rv.setRemoteAdapter(android.R.id.list, adapter)
            appWidgetManager.updateAppWidget(widgetID, rv)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, android.R.id.list)
        }
    }
}
