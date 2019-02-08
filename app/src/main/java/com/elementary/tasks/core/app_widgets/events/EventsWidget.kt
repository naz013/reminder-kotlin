package com.elementary.tasks.core.app_widgets.events

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.app_widgets.buttons.VoiceWidgetDialog
import com.elementary.tasks.reminder.create.CreateReminderActivity
import java.text.SimpleDateFormat
import java.util.*

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

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val sp = context.getSharedPreferences(
                EventsWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)

        for (i in appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val editor = context.getSharedPreferences(
                EventsWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit()
        for (widgetID in appWidgetIds) {
            editor.remove(EventsWidgetConfigActivity.WIDGET_HEADER_BG + widgetID)
            editor.remove(EventsWidgetConfigActivity.WIDGET_ITEM_BG + widgetID)
            editor.remove(EventsWidgetConfigActivity.WIDGET_TEXT_SIZE + widgetID)
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
            rv.setTextViewText(R.id.widgetTitle, date)

            val headerBgColor = sp.getInt(EventsWidgetConfigActivity.WIDGET_HEADER_BG + widgetID, 0)

            rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))

            if (WidgetUtils.isDarkBg(headerBgColor)) {
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_settings_white, R.id.btn_settings, EventsWidgetConfigActivity::class.java) {
                    it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
                    return@initButton it
                }
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_add_white, R.id.btn_add_task, CreateReminderActivity::class.java)
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_mic_white, R.id.btn_voice, VoiceWidgetDialog::class.java)
                rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
            } else {
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_settings_24px, R.id.btn_settings, EventsWidgetConfigActivity::class.java) {
                    it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
                    return@initButton it
                }
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_add_24px, R.id.btn_add_task, CreateReminderActivity::class.java)
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_mic_24px, R.id.btn_voice, VoiceWidgetDialog::class.java)
                rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
            }

            val startActivityIntent = Intent(context, EventActionReceiver::class.java)
            val startActivityPendingIntent = PendingIntent.getBroadcast(context, 0, startActivityIntent, 0)
            rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

            val adapter = Intent(context, EventsService::class.java)
            adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            rv.setRemoteAdapter(android.R.id.list, adapter)
            appWidgetManager.updateAppWidget(widgetID, rv)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, android.R.id.list)
        }
    }
}
