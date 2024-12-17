package com.elementary.tasks.core.appwidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.appwidgets.WidgetPrefsHolder
import com.elementary.tasks.core.services.BaseBroadcast
import com.github.naz013.logging.Logger
import org.koin.core.component.inject
import org.threeten.bp.LocalDate

class CalendarPreviousReceiver : BaseBroadcast() {

  private val widgetPrefsHolder by inject<WidgetPrefsHolder>()

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
      updatesHelper.updateCalendarWidget()
    }
  }

  companion object {
    const val ACTION_PREVIOUS = "com.elementary.tasks.core.app_widgets.calendar.ACTION_PREVIOUS"
  }
}
