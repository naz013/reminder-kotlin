package com.github.naz013.appwidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetInteractedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.WidgetPrefsHolder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.threeten.bp.LocalDate

internal class CalendarNextReceiver : BroadcastReceiver(), KoinComponent {

  private val widgetPrefsHolder by inject<WidgetPrefsHolder>()
  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()

  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent != null && ACTION_NEXT == intent.action && context != null) {
      val widgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
      )

      val prefsProvider = widgetPrefsHolder.findOrCreate(widgetId)

      val year = prefsProvider.getYear()
      val month = prefsProvider.getMonth() + 1
      val date = LocalDate.of(year, month, 15).plusMonths(1)

      prefsProvider.setMonth(date.monthValue - 1)
      prefsProvider.setYear(date.year)
      appWidgetUpdater.updateCalendarWidget()
      analyticsEventSender.send(WidgetInteractedEvent(Widget.CALENDAR))
    }
  }

  companion object {
    const val ACTION_NEXT = "com.elementary.tasks.core.app_widgets.calendar.ACTION_NEXT"
  }
}
