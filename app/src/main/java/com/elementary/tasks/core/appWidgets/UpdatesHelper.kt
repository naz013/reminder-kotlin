package com.elementary.tasks.core.appWidgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent

import com.elementary.tasks.core.appWidgets.calendar.CalendarWidget
import com.elementary.tasks.core.appWidgets.events.EventsWidget
import com.elementary.tasks.core.appWidgets.notes.NotesWidget
import com.elementary.tasks.core.appWidgets.tasks.TasksWidget

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
class UpdatesHelper private constructor(context: Context) : ContextWrapper(context) {

    fun updateWidget() {
        val intent = Intent(applicationContext, EventsWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, EventsWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
        updateCalendarWidget()
        updateTasksWidget()
    }

    fun updateNotesWidget() {
        val intent = Intent(applicationContext, NotesWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, NotesWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    fun updateCalendarWidget() {
        val intent = Intent(applicationContext, CalendarWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, CalendarWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    fun updateTasksWidget() {
        val intent = Intent(applicationContext, TasksWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, TasksWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    companion object {

        private var helper: UpdatesHelper? = null

        fun getInstance(context: Context): UpdatesHelper {
            if (helper == null || helper!!.applicationContext == null) {
                helper = UpdatesHelper(context)
            }
            return helper!!
        }
    }
}
