package com.github.naz013.appwidgets.events

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
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale

internal class EventsWidget : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (id in appWidgetIds) {
      updateWidget(context, appWidgetManager, EventsWidgetPrefsProvider(context, id))
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  companion object {

    fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      prefsProvider: EventsWidgetPrefsProvider
    ) {
      val cal = GregorianCalendar()
      val dateFormat = SimpleDateFormat("EEE, dd MMMM yyyy", Locale.getDefault())
      dateFormat.calendar = cal
      val date = dateFormat.format(cal.time)

      val rv = RemoteViews(context.packageName, R.layout.widget_current_tasks)
      rv.setTextViewText(R.id.widgetTitle, date)

      val headerBgColor = prefsProvider.getHeaderBackground()

      rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))

      if (WidgetUtils.isDarkBg(headerBgColor)) {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_settings,
          color = R.color.pureWhite,
          viewId = R.id.btn_settings,
          cls = EventsWidgetConfigActivity::class.java
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
          intent = createIntent(context, Direction.ADD_REMINDER)
        )
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
      } else {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_settings,
          color = R.color.pureBlack,
          viewId = R.id.btn_settings,
          cls = EventsWidgetConfigActivity::class.java
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
          intent = createIntent(context, Direction.ADD_REMINDER)
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

      val adapter = Intent(context, EventsService::class.java)
      adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
      rv.setRemoteAdapter(android.R.id.list, adapter)
      appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
      appWidgetManager.notifyAppWidgetViewDataChanged(prefsProvider.widgetId, android.R.id.list)
    }

    private fun createIntent(context: Context, direction: Direction): Intent {
      return AppWidgetActionActivity.createIntent(context).apply {
        putExtra(AppWidgetActionActivity.DIRECTION, direction)
      }
    }
  }
}
