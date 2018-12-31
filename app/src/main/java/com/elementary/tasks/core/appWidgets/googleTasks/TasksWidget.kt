package com.elementary.tasks.core.appWidgets.googleTasks

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.WidgetUtils
import com.elementary.tasks.googleTasks.create.TaskActivity
import com.elementary.tasks.googleTasks.create.TasksConstants

/**
 * Copyright 2016 Nazar Suhovich
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
class TasksWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val sp = context.getSharedPreferences(
                TasksWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
        for (i in appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val editor = context.getSharedPreferences(
                TasksWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit()
        for (widgetID in appWidgetIds) {
            editor.remove(TasksWidgetConfigActivity.WIDGET_HEADER_BG + widgetID)
            editor.remove(TasksWidgetConfigActivity.WIDGET_ITEM_BG + widgetID)
        }
        editor.apply()
    }

    companion object {

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                         sp: SharedPreferences, widgetID: Int) {
            val rv = RemoteViews(context.packageName, R.layout.widget_google_tasks)

            val headerBgColor = sp.getInt(TasksWidgetConfigActivity.WIDGET_HEADER_BG + widgetID, 0)

            rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))

            if (WidgetUtils.isDarkBg(headerBgColor)) {
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_settings_white, R.id.btn_settings, TasksWidgetConfigActivity::class.java) {
                    it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
                    return@initButton it
                }
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_add_white, R.id.btn_add_task, TaskActivity::class.java) {
                    it.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE)
                    return@initButton it
                }
                rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
            } else {
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_settings_24px, R.id.btn_settings, TasksWidgetConfigActivity::class.java) {
                    it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
                    return@initButton it
                }
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_add_24px, R.id.btn_add_task, TaskActivity::class.java) {
                    it.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE)
                    return@initButton it
                }
                rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
            }

            val startActivityIntent = Intent(context, TaskActivity::class.java)
            val startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                    startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

            val adapter = Intent(context, TasksService::class.java)
            adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            rv.setRemoteAdapter(android.R.id.list, adapter)
            appWidgetManager.updateAppWidget(widgetID, rv)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, android.R.id.list)
        }
    }
}
