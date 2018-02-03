package com.elementary.tasks.core.app_widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.elementary.tasks.core.app_widgets.calendar.CalendarWidget;
import com.elementary.tasks.core.app_widgets.events.EventsWidget;
import com.elementary.tasks.core.app_widgets.notes.NotesWidget;
import com.elementary.tasks.core.app_widgets.tasks.TasksWidget;

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
public final class UpdatesHelper extends ContextWrapper {

    private static UpdatesHelper helper;

    private UpdatesHelper(Context context) {
        super(context);
    }

    public static UpdatesHelper getInstance(Context context) {
        if (helper == null || helper.getApplicationContext() == null) {
            helper = new UpdatesHelper(context);
        }
        return helper;
    }

    public void updateWidget() {
        Intent intent = new Intent(getApplicationContext(), EventsWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new
                ComponentName(getApplicationContext(), EventsWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
        updateCalendarWidget();
        updateTasksWidget();
    }

    public void updateNotesWidget() {
        Intent intent = new Intent(getApplicationContext(), NotesWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new
                ComponentName(getApplicationContext(), NotesWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    public void updateCalendarWidget() {
        Intent intent = new Intent(getApplicationContext(), CalendarWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new
                ComponentName(getApplicationContext(), CalendarWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    public void updateTasksWidget() {
        Intent intent = new Intent(getApplicationContext(), TasksWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new
                ComponentName(getApplicationContext(), TasksWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}
