package com.elementary.tasks.core.app_widgets.tasks;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TaskListItem;
import com.elementary.tasks.google_tasks.TasksConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
class TasksFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int widgetID;
    private ThemeUtil cs;
    @NonNull
    private List<TaskItem> mData = new ArrayList<>();
    @NonNull
    private Map<String, Integer> map = new HashMap<>();

    TasksFactory(Context ctx, Intent intent) {
        mContext = ctx;
        widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mData.clear();
        map.clear();
        cs = ThemeUtil.getInstance(mContext);
    }

    @Override
    public void onDataSetChanged() {
        map.clear();
        List<TaskListItem> list = RealmDb.getInstance().getTaskLists();
        for (TaskListItem item : list) {
            map.put(item.getListId(), item.getColor());
        }
        mData.clear();
        mData.addAll(RealmDb.getInstance().getTasks(null));
    }

    @Override
    public void onDestroy() {
        map.clear();
        mData.clear();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(
                TasksWidgetConfig.TASKS_WIDGET_PREF, Context.MODE_PRIVATE);
        RemoteViews rView = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_tasks_widget);
        if (i >= getCount()) {
            rView.setTextViewText(R.id.task, mContext.getString(R.string.failed_to_load));
            rView.setTextViewText(R.id.note, "");
            rView.setTextViewText(R.id.taskDate, "");
            return rView;
        }
        int theme = sp.getInt(TasksWidgetConfig.TASKS_WIDGET_THEME + widgetID, 0);
        TasksTheme tasksTheme = TasksTheme.getThemes(mContext).get(theme);
        int itemTextColor = tasksTheme.getItemTextColor();
        rView.setTextColor(R.id.task, itemTextColor);
        rView.setTextColor(R.id.note, itemTextColor);
        rView.setTextColor(R.id.taskDate, itemTextColor);
        rView.setViewVisibility(R.id.checkDone, View.GONE);
        if (map.containsKey(mData.get(i).getListId())) {
            rView.setInt(R.id.listColor, "setBackgroundColor", cs.getNoteColor(map.get(mData.get(i).getListId())));
        }
        String name = mData.get(i).getTitle();
        rView.setTextViewText(R.id.task, name);
        SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());
        String notes = mData.get(i).getNotes();
        if (notes != null && !notes.matches("")) {
            rView.setTextViewText(R.id.note, notes);
        } else {
            rView.setViewVisibility(R.id.note, View.GONE);
        }

        long date = mData.get(i).getDueDate();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (date != 0) {
            calendar.setTimeInMillis(date);
            String update = full24Format.format(calendar.getTime());
            rView.setTextViewText(R.id.taskDate, update);
        } else {
            rView.setViewVisibility(R.id.taskDate, View.GONE);
        }

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.INTENT_ID, mData.get(i).getTaskId());
        fillInIntent.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT);
        rView.setOnClickFillInIntent(R.id.task, fillInIntent);
        rView.setOnClickFillInIntent(R.id.note, fillInIntent);
        rView.setOnClickFillInIntent(R.id.taskDate, fillInIntent);
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}