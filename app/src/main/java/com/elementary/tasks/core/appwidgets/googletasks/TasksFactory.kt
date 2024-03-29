package com.elementary.tasks.core.appwidgets.googletasks

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.appwidgets.AppWidgetActionActivity
import com.elementary.tasks.core.appwidgets.Direction
import com.elementary.tasks.core.appwidgets.WidgetIntentProtocol
import com.elementary.tasks.core.appwidgets.WidgetUtils
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.googletasks.TasksConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksFactory(
  private val context: Context,
  intent: Intent,
  private val appDb: AppDb
) : RemoteViewsService.RemoteViewsFactory {

  private val widgetID: Int = intent.getIntExtra(
    AppWidgetManager.EXTRA_APPWIDGET_ID,
    AppWidgetManager.INVALID_APPWIDGET_ID
  )
  private val prefsProvider = GoogleTasksWidgetPrefsProvider(context, widgetID)

  private val mData = mutableListOf<GoogleTask>()
  private val map = mutableMapOf<String, Int>()

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
    val rv = RemoteViews(context.packageName, R.layout.list_item_widget_google_task)

    rv.setTextViewText(R.id.note, "")
    rv.setTextViewText(R.id.taskDate, "")

    if (i >= count) {
      rv.setTextViewText(R.id.task, context.getString(R.string.failed_to_load))
      return rv
    }
    val itemBgColor = prefsProvider.getItemBackground()

    rv.setInt(R.id.listItemCard, "setBackgroundResource", WidgetUtils.newWidgetBg(itemBgColor))

    if (WidgetUtils.isDarkBg(itemBgColor)) {
      rv.setTextColor(R.id.task, ContextCompat.getColor(context, R.color.pureWhite))
      rv.setTextColor(R.id.note, ContextCompat.getColor(context, R.color.pureWhite))
      rv.setTextColor(R.id.taskDate, ContextCompat.getColor(context, R.color.pureWhite))
    } else {
      rv.setTextColor(R.id.task, ContextCompat.getColor(context, R.color.pureBlack))
      rv.setTextColor(R.id.note, ContextCompat.getColor(context, R.color.pureBlack))
      rv.setTextColor(R.id.taskDate, ContextCompat.getColor(context, R.color.pureBlack))
    }

    val task = mData[i]
    val listColor = if (map.containsKey(task.listId)) {
      ThemeProvider.themedColor(context, map[task.listId] ?: 0)
    } else {
      ThemeProvider.themedColor(context, 0)
    }

    val icon = if (task.status == GTasks.TASKS_COMPLETE) {
      ViewUtils.createIcon(context, R.drawable.ic_builder_google_task_list, listColor)
    } else {
      ViewUtils.createIcon(context, R.drawable.ic_fluent_radio_button, listColor)
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

    val data = WidgetIntentProtocol(
      mapOf<String, Any?>(
        Pair(Constants.INTENT_ID, task.taskId),
        Pair(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)
      )
    )

    val fillInIntent = Intent()
    fillInIntent.putExtra(AppWidgetActionActivity.DATA, data)
    fillInIntent.putExtra(AppWidgetActionActivity.DIRECTION, Direction.GOOGLE_TASK)
    rv.setOnClickFillInIntent(R.id.listItemCard, fillInIntent)
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
