package com.elementary.tasks.core.appWidgets.buttons

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes

import com.elementary.tasks.R
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.appWidgets.WidgetUtils
import com.elementary.tasks.core.appWidgets.voiceControl.VoiceWidgetDialog
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity

/**
 * Copyright 2018 Nazar Suhovich
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
class CombinedButtonsWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val sp = context.getSharedPreferences(
                CombinedWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
        for (i in appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i)
        }
    }

    companion object {

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                         sp: SharedPreferences, widgetID: Int) {
            val color = sp.getInt(CombinedWidgetConfigActivity.WIDGET_BG_COLOR + widgetID, 0)

            val rv = RemoteViews(context.packageName, R.layout.widget_combined_buttons)
            rv.setInt(R.id.widgetBg, "setBackgroundResource", WidgetUtils.newWidgetBg(color))

            if (WidgetUtils.isDarkBg(color)) {
                initButton(context, rv, R.drawable.ic_twotone_alarm_white, R.id.btn_add_reminder, CreateReminderActivity::class.java)
                initButton(context, rv, R.drawable.ic_twotone_note_white, R.id.btn_add_note, CreateNoteActivity::class.java)
                initButton(context, rv, R.drawable.ic_twotone_cake_white, R.id.btn_add_birthday, AddBirthdayActivity::class.java)
                initButton(context, rv, R.drawable.ic_twotone_mic_white, R.id.btn_voice, VoiceWidgetDialog::class.java)
            } else {
                initButton(context, rv, R.drawable.ic_twotone_alarm_24px, R.id.btn_add_reminder, CreateReminderActivity::class.java)
                initButton(context, rv, R.drawable.ic_twotone_note_24px, R.id.btn_add_note, CreateNoteActivity::class.java)
                initButton(context, rv, R.drawable.ic_twotone_cake_24px, R.id.btn_add_birthday, AddBirthdayActivity::class.java)
                initButton(context, rv, R.drawable.ic_twotone_mic_24px, R.id.btn_voice, VoiceWidgetDialog::class.java)
            }

            appWidgetManager.updateAppWidget(widgetID, rv)
        }

        private fun initButton(context: Context, rv: RemoteViews, @DrawableRes iconId: Int, @IdRes viewId: Int, cls: Class<*>) {
            val configIntent = Intent(context, cls)
            val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(viewId, configPendingIntent)
            WidgetUtils.setIcon(context, rv, iconId, viewId)
        }
    }
}
