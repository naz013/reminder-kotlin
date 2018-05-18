package com.elementary.tasks.core.app_widgets.tasks;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.widget.RemoteViews;

import com.elementary.tasks.R;
import com.elementary.tasks.core.app_widgets.WidgetUtils;
import com.elementary.tasks.google_tasks.TaskActivity;
import com.elementary.tasks.google_tasks.TasksConstants;

/**
 * Copyright 2016 Nazar Suhovich
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

public class TasksWidget extends AppWidgetProvider {

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences sp = context.getSharedPreferences(
                TasksWidgetConfig.TASKS_WIDGET_PREF, Context.MODE_PRIVATE);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.tasks_widget_layout);
        int theme = sp.getInt(TasksWidgetConfig.TASKS_WIDGET_THEME + widgetID, 0);
        TasksTheme tasksTheme = TasksTheme.getThemes(context).get(theme);
        int headerColor = tasksTheme.getHeaderColor();
        int backgroundColor = tasksTheme.getBackgroundColor();
        int titleColor = tasksTheme.getTitleColor();
        int plusIcon = tasksTheme.getPlusIcon();
        int settingsIcon = tasksTheme.getSettingsIcon();

        rv.setInt(R.id.headerBg, "setBackgroundResource", headerColor);
        rv.setInt(R.id.widgetBg, "setBackgroundResource", backgroundColor);
        rv.setTextColor(R.id.widgetTitle, titleColor);
        WidgetUtils.setIcon(context, rv, plusIcon, R.id.tasksCount);

        Intent configIntent = new Intent(context, TaskActivity.class);
        configIntent.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.tasksCount, configPendingIntent);

        configIntent = new Intent(context, TasksWidgetConfig.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent);
        WidgetUtils.setIcon(context, rv, settingsIcon, R.id.settingsButton);

        Intent startActivityIntent = new Intent(context, TaskActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent);

        Intent adapter = new Intent(context, TasksService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        rv.setRemoteAdapter(android.R.id.list, adapter);
        appWidgetManager.updateAppWidget(widgetID, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, android.R.id.list);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences.Editor editor = context.getSharedPreferences(
                TasksWidgetConfig.TASKS_WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(TasksWidgetConfig.TASKS_WIDGET_THEME + widgetID);
        }
        editor.apply();
    }
}
