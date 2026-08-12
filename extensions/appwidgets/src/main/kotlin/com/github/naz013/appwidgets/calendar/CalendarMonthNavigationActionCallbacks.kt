package com.github.naz013.appwidgets.calendar

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetInteractedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.threeten.bp.LocalDate

internal class CalendarPreviousMonthActionCallback : ActionCallback, KoinComponent {

  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()

  override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
    val prefsProvider = CalendarWidgetPrefsProvider(context, widgetId)

    val year = prefsProvider.getResolvedYear()
    val month = prefsProvider.getMonth() + 1
    val date = LocalDate.of(year, month, 15).minusMonths(1)

    prefsProvider.setMonth(date.monthValue - 1)
    prefsProvider.setYear(date.year)

    appWidgetUpdater.updateCalendarWidget(widgetId)
    analyticsEventSender.send(WidgetInteractedEvent(Widget.CALENDAR))
  }
}

internal class CalendarNextMonthActionCallback : ActionCallback, KoinComponent {

  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()

  override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
    val prefsProvider = CalendarWidgetPrefsProvider(context, widgetId)

    val year = prefsProvider.getResolvedYear()
    val month = prefsProvider.getMonth() + 1
    val date = LocalDate.of(year, month, 15).plusMonths(1)

    prefsProvider.setMonth(date.monthValue - 1)
    prefsProvider.setYear(date.year)

    appWidgetUpdater.updateCalendarWidget(widgetId)
    analyticsEventSender.send(WidgetInteractedEvent(Widget.CALENDAR))
  }
}
