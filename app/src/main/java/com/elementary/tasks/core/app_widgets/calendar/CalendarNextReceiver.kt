package com.elementary.tasks.core.app_widgets.calendar

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import timber.log.Timber

class CalendarNextReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    Timber.d("onReceive: $intent")
    if (intent != null && ACTION_NEXT == intent.action) {
      val action = intent.getIntExtra(ARG_VALUE, 0)
      val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID)
      val sp = context?.getSharedPreferences(CalendarWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
        ?: return
      var month = sp.getInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_MONTH + widgetId, 0)
      var year = sp.getInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_YEAR + widgetId, 0)
      if (action != 0) {
        val editor = sp.edit()
        if (month in 0..10) {
          month += 1
        } else {
          month = 0
        }
        editor.putInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_MONTH + widgetId, month)
        if (month == 0) {
          year += 1
        }
        editor.putInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_YEAR + widgetId, year)
        editor.apply()
        UpdatesHelper.updateCalendarWidget(context)
      }
    }
  }

  companion object {
    const val ACTION_NEXT = "com.elementary.tasks.core.app_widgets.calendar.ACTION_NEXT"
    const val ARG_VALUE = "action_value"
  }
}
