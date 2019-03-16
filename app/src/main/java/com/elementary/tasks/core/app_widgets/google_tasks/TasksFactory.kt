package com.elementary.tasks.core.app_widgets.google_tasks

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.google_tasks.create.TasksConstants
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
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
class TasksFactory(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory, KoinComponent {

    private val widgetID: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID)
    private val themeUtil: ThemeUtil by inject()
    private val appDb: AppDb by inject()
    private val mData = ArrayList<GoogleTask>()
    private val map = HashMap<String, Int>()

    override fun onCreate() {
        mData.clear()
        map.clear()
    }

    override fun onDataSetChanged() {
        map.clear()
        val list = appDb.googleTaskListsDao().all()
        for (item in list) {
            map[item.listId] = item.color
        }
        mData.clear()
        mData.addAll(appDb.googleTasksDao().all())
    }

    override fun onDestroy() {
        map.clear()
        mData.clear()
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getViewAt(i: Int): RemoteViews {
        val sp = mContext.getSharedPreferences(TasksWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
        val rv = RemoteViews(mContext.packageName, R.layout.list_item_widget_google_task)

        rv.setTextViewText(R.id.note, "")
        rv.setTextViewText(R.id.taskDate, "")

        if (i >= count) {
            rv.setTextViewText(R.id.task, mContext.getString(R.string.failed_to_load))
            return rv
        }
        val itemBgColor = sp.getInt(TasksWidgetConfigActivity.WIDGET_ITEM_BG + widgetID, 0)

        rv.setInt(R.id.listItemCard, "setBackgroundResource", WidgetUtils.newWidgetBg(itemBgColor))

        if (WidgetUtils.isDarkBg(itemBgColor)) {
            rv.setTextColor(R.id.task, ContextCompat.getColor(mContext, R.color.pureWhite))
            rv.setTextColor(R.id.note, ContextCompat.getColor(mContext, R.color.pureWhite))
            rv.setTextColor(R.id.taskDate, ContextCompat.getColor(mContext, R.color.pureWhite))
        } else {
            rv.setTextColor(R.id.task, ContextCompat.getColor(mContext, R.color.pureBlack))
            rv.setTextColor(R.id.note, ContextCompat.getColor(mContext, R.color.pureBlack))
            rv.setTextColor(R.id.taskDate, ContextCompat.getColor(mContext, R.color.pureBlack))
        }

        val task = mData[i]
        val listColor = if (map.containsKey(task.listId)) {
            themeUtil.getNoteLightColor(map[task.listId] ?: 0)
        } else {
            themeUtil.getNoteLightColor(0)
        }

        val icon = if (task.status == GTasks.TASKS_COMPLETE) {
            ViewUtils.createIcon(mContext, R.drawable.ic_check, listColor)
        } else {
            ViewUtils.createIcon(mContext, R.drawable.ic_empty_circle, listColor)
        }
        rv.setImageViewBitmap(R.id.statusIcon, icon)

        rv.setTextViewText(R.id.task, task.title)
        val full24Format = SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault())

        val notes = task.notes
        if (notes.isNotBlank()) {
            rv.setTextViewText(R.id.note, notes)
            rv.setViewVisibility(R.id.note, View.VISIBLE)
        } else {
            rv.setViewVisibility(R.id.note, View.GONE)
        }

        val date = task.dueDate
        val calendar = Calendar.getInstance()
        if (date != 0L) {
            calendar.timeInMillis = date
            val update = full24Format.format(calendar.time)
            rv.setTextViewText(R.id.taskDate, update)
            rv.setViewVisibility(R.id.taskDate, View.VISIBLE)
        } else {
            rv.setViewVisibility(R.id.taskDate, View.GONE)
        }

        val fillInIntent = Intent()
        fillInIntent.putExtra(Constants.INTENT_ID, task.taskId)
        fillInIntent.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)
        rv.setOnClickFillInIntent(R.id.task, fillInIntent)
        rv.setOnClickFillInIntent(R.id.note, fillInIntent)
        rv.setOnClickFillInIntent(R.id.taskDate, fillInIntent)
        return rv
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