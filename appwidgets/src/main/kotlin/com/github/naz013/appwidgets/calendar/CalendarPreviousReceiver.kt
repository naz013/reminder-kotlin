package com.github.naz013.appwidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.WidgetPrefsHolder
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.threeten.bp.LocalDate

internal class CalendarPreviousReceiver : BroadcastReceiver(), KoinComponent {

  private val widgetPrefsHolder by inject<WidgetPrefsHolder>()
  private val appWidgetUpdater by inject<AppWidgetUpdater>()

  override fun onReceive(context: Context?, intent: Intent?) {
    Logger.d("onReceive: $intent")
    if (intent != null && ACTION_PREVIOUS == intent.action && context != null) {
      val widgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
      )

      val prefsProvider = widgetPrefsHolder.findOrCreate(
        widgetId,
        CalendarWidgetPrefsProvider::class.java
      )

      val year = prefsProvider.getYear()
      val month = prefsProvider.getMonth() + 1

      val date = LocalDate.of(
        /* year = */ year,
        /* month = */ month,
        /* dayOfMonth = */ 15
      ).minusMonths(1)

      prefsProvider.setMonth(date.monthValue - 1)
      prefsProvider.setYear(date.year)
      appWidgetUpdater.updateCalendarWidget()
    }
  }

  companion object {
    const val ACTION_PREVIOUS = "com.elementary.tasks.core.app_widgets.calendar.ACTION_PREVIOUS"
  }
}
