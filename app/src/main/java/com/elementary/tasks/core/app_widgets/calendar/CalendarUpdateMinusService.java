package com.elementary.tasks.core.app_widgets.calendar;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.elementary.tasks.core.app_widgets.UpdatesHelper;

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

public class CalendarUpdateMinusService extends IntentService {

    public CalendarUpdateMinusService() {
        super("CalendarUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int action = intent.getIntExtra("actionMinus", 0);
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        SharedPreferences sp = getSharedPreferences(CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        int month  = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetId, 0);
        int year  = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetId, 0);
        if (action != 0){
            SharedPreferences.Editor editor = sp.edit();
            if (month == 0) month = 11;
            else month -= 1;
            editor.putInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetId, month);
            if (month == 11) year -= 1;
            editor.putInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetId, year);
            editor.apply();
            UpdatesHelper.getInstance(getApplicationContext()).updateCalendarWidget();
            stopSelf();
        } else stopSelf();
    }
}
