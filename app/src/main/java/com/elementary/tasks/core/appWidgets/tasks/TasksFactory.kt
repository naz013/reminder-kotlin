package com.elementary.tasks.core.appWidgets.tasks

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.googleTasks.create.TasksConstants
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

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
class TasksFactory(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private val widgetID: Int = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID)
    @Inject lateinit var themeUtil: ThemeUtil
    private val mData = ArrayList<GoogleTask>()
    private val map = HashMap<String, Int>()

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate() {
        mData.clear()
        map.clear()
    }

    override fun onDataSetChanged() {
        map.clear()
        val list = AppDb.getAppDatabase(mContext).googleTaskListsDao().all()
        for (item in list) {
            map[item.listId] = item.color
        }
        mData.clear()
        mData.addAll(AppDb.getAppDatabase(mContext).googleTasksDao().all())
    }

    override fun onDestroy() {
        map.clear()
        mData.clear()
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getViewAt(i: Int): RemoteViews {
        val sp = mContext.getSharedPreferences(
                TasksWidgetConfig.TASKS_WIDGET_PREF, Context.MODE_PRIVATE)
        val rView = RemoteViews(mContext.packageName,
                R.layout.list_item_tasks_widget)
        if (i >= count) {
            rView.setTextViewText(R.id.task, mContext.getString(R.string.failed_to_load))
            rView.setTextViewText(R.id.note, "")
            rView.setTextViewText(R.id.taskDate, "")
            return rView
        }
        val theme = sp.getInt(TasksWidgetConfig.TASKS_WIDGET_THEME + widgetID, 0)
        val tasksTheme = TasksTheme.getThemes(mContext)[theme]
        val itemTextColor = tasksTheme.itemTextColor
        rView.setTextColor(R.id.task, itemTextColor)
        rView.setTextColor(R.id.note, itemTextColor)
        rView.setTextColor(R.id.taskDate, itemTextColor)
        rView.setViewVisibility(R.id.checkDone, View.GONE)
        if (map.containsKey(mData[i].listId)) {
            rView.setInt(R.id.listColor, "setBackgroundColor", themeUtil.getNoteColor(map[mData[i].listId]!!))
        }
        val name = mData[i].title
        rView.setTextViewText(R.id.task, name)
        val full24Format = SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault())
        val notes = mData[i].notes
        if (!notes.matches("".toRegex())) {
            rView.setTextViewText(R.id.note, notes)
        } else {
            rView.setViewVisibility(R.id.note, View.GONE)
        }

        val date = mData[i].dueDate
        val calendar = java.util.Calendar.getInstance()
        if (date != 0L) {
            calendar.timeInMillis = date
            val update = full24Format.format(calendar.time)
            rView.setTextViewText(R.id.taskDate, update)
        } else {
            rView.setViewVisibility(R.id.taskDate, View.GONE)
        }

        val fillInIntent = Intent()
        fillInIntent.putExtra(Constants.INTENT_ID, mData[i].taskId)
        fillInIntent.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)
        rView.setOnClickFillInIntent(R.id.task, fillInIntent)
        rView.setOnClickFillInIntent(R.id.note, fillInIntent)
        rView.setOnClickFillInIntent(R.id.taskDate, fillInIntent)
        return rView
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}