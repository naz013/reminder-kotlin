package com.elementary.tasks.core.app_widgets.calendar

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

import com.elementary.tasks.core.app_widgets.UpdatesHelper

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

class CalendarUpdateMinusService : IntentService("CalendarUpdateService") {

    override fun onHandleIntent(intent: Intent?) {
        val action = intent!!.getIntExtra("actionMinus", 0)
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        val sp = getSharedPreferences(CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE)
        var month = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetId, 0)
        var year = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetId, 0)
        if (action != 0) {
            val editor = sp.edit()
            if (month == 0) {
                month = 11
            } else {
                month -= 1
            }
            editor.putInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetId, month)
            if (month == 11) {
                year -= 1
            }
            editor.putInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetId, year)
            editor.apply()
            UpdatesHelper.getInstance(applicationContext).updateCalendarWidget()
            stopSelf()
        } else {
            stopSelf()
        }
    }
}
