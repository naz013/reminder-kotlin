package com.github.naz013.appwidgets.googletasks

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.GoogleTask
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.usecase.googletasks.GetAllGoogleTaskListsUseCase
import com.github.naz013.usecase.googletasks.GetAllGoogleTasksUseCase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal class TasksFactory(
  private val context: Context,
  intent: Intent,
  private val getAllGoogleTaskListsUseCase: GetAllGoogleTaskListsUseCase,
  private val getAllGoogleTasksUseCase: GetAllGoogleTasksUseCase
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
    val list = invokeSuspend { getAllGoogleTaskListsUseCase() }
    for (item in list) {
      map[item.listId] = item.color
    }
    mData.clear()
    mData.addAll(invokeSuspend { getAllGoogleTasksUseCase() })
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

    val icon = if (task.status == GoogleTask.TASKS_COMPLETE) {
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
        Pair(IntentKeys.INTENT_ID, task.taskId)
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
