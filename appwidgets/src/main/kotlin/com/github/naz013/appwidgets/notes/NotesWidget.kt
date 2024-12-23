package com.github.naz013.appwidgets.notes

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
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.intent.PendingIntentWrapper

internal class NotesWidget : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (id in appWidgetIds) {
      updateWidget(context, appWidgetManager, NotesWidgetPrefsProvider(context, id))
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  companion object {

    fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      prefsProvider: NotesWidgetPrefsProvider
    ) {
      val rv = RemoteViews(context.packageName, R.layout.widget_note)
      val headerBgColor = prefsProvider.getHeaderBackground()

      rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))

      val tintColor = if (WidgetUtils.isDarkBg(headerBgColor)) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }

      WidgetUtils.initButton(
        context = context,
        rv = rv,
        iconId = R.drawable.ic_fluent_settings,
        color = tintColor,
        viewId = R.id.btn_settings,
        cls = NotesWidgetConfigActivity::class.java
      ) {
        it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
        return@initButton it
      }
      WidgetUtils.initButton(
        context = context,
        rv = rv,
        iconId = R.drawable.ic_builder_google_task_list,
        color = tintColor,
        viewId = R.id.btn_add_note,
        intent = createIntent(context)
      )
      rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, tintColor))

      val startActivityIntent = AppWidgetActionActivity.createIntent(context)
      val startActivityPendingIntent = PendingIntentWrapper.getActivity(
        context = context,
        requestCode = RandomRequestCode.generate(),
        intent = startActivityIntent,
        flags = PendingIntent.FLAG_MUTABLE,
        ignoreIn13 = true
      )
      rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

      val adapter = Intent(context, NotesService::class.java)
      adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
      rv.setRemoteAdapter(android.R.id.list, adapter)

      runCatching {
        appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
        appWidgetManager.notifyAppWidgetViewDataChanged(prefsProvider.widgetId, android.R.id.list)
      }
    }

    private fun createIntent(context: Context): Intent {
      return AppWidgetActionActivity.createIntent(context).apply {
        putExtra(AppWidgetActionActivity.DIRECTION, Direction.NOTE_PREVIEW)
      }
    }
  }
}
