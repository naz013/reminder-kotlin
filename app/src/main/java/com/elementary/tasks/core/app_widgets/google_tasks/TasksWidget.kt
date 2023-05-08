package com.elementary.tasks.core.app_widgets.google_tasks

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.AppWidgetActionActivity
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.os.PendingIntentWrapper
import com.elementary.tasks.google_tasks.TasksConstants
import com.elementary.tasks.google_tasks.task.GoogleTaskActivity

class TasksWidget : AppWidgetProvider() {

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

      if (WidgetUtils.isDarkBg(headerBgColor)) {
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_settings_24px, R.color.pureWhite,
          R.id.btn_settings, TasksWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
          return@initButton it
        }
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_add_24px, R.color.pureWhite,
          R.id.btn_add_task, GoogleTaskActivity::class.java
        ) {
          it.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE)
          return@initButton it
        }
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
      } else {
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_settings_24px, R.color.pureBlack,
          R.id.btn_settings, TasksWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
          return@initButton it
        }
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_add_24px, R.color.pureBlack,
          R.id.btn_add_task, GoogleTaskActivity::class.java
        ) {
          it.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE)
          return@initButton it
        }
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
      }

      val startActivityIntent = AppWidgetActionActivity.createIntent(context)
      val startActivityPendingIntent = PendingIntentWrapper.getActivity(
        context,
        0,
        startActivityIntent,
        PendingIntent.FLAG_MUTABLE,
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
