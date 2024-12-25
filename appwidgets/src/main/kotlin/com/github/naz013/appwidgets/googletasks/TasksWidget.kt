package com.github.naz013.appwidgets.googletasks

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.RandomRequestCode
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.usecase.googletasks.TasksIntentKeys

internal class TasksWidget : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (id in appWidgetIds) {
      updateWidget(context, appWidgetManager, GoogleTasksWidgetPrefsProvider(context, id))
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  companion object {

    fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      prefsProvider: GoogleTasksWidgetPrefsProvider
    ) {
      val rv = RemoteViews(context.packageName, R.layout.widget_google_tasks)

      val headerBgColor = prefsProvider.getHeaderBackground()

      rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))

      val createTaskIntent = AppWidgetActionActivity.createIntent(context).apply {
        val data = WidgetIntentProtocol(
          mapOf<String, Any?>(
            Pair(IntentKeys.INTENT_ID, ""),
            Pair(TasksIntentKeys.INTENT_ACTION, TasksIntentKeys.CREATE)
          )
        )
        putExtra(AppWidgetActionActivity.DATA, data)
        putExtra(AppWidgetActionActivity.DIRECTION, Direction.GOOGLE_TASK)
      }
      if (WidgetUtils.isDarkBg(headerBgColor)) {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_settings,
          color = R.color.pureWhite,
          viewId = R.id.btn_settings,
          cls = TasksWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
          return@initButton it
        }
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_add,
          color = R.color.pureWhite,
          viewId = R.id.btn_add_task,
          intent = createTaskIntent
        )
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
      } else {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_settings,
          color = R.color.pureBlack,
          viewId = R.id.btn_settings,
          cls = TasksWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
          return@initButton it
        }
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_add,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_task,
          intent = createTaskIntent
        )
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
      }

      val startActivityIntent = AppWidgetActionActivity.createIntent(context)
      val startActivityPendingIntent = PendingIntentWrapper.getActivity(
        context = context,
        requestCode = RandomRequestCode.generate(),
        intent = startActivityIntent,
        flags = PendingIntent.FLAG_MUTABLE,
        ignoreIn13 = true
      )
      rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

      val adapter = Intent(context, TasksService::class.java)
      adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
      rv.setRemoteAdapter(android.R.id.list, adapter)
      appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
      appWidgetManager.notifyAppWidgetViewDataChanged(prefsProvider.widgetId, android.R.id.list)
    }
  }
}
