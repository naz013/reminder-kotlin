package com.github.naz013.appwidgets

import android.content.Context
import com.github.naz013.appwidgets.calendar.CalendarWidgetPrefsProvider
import com.github.naz013.logging.Logger

internal class WidgetPrefsHolder(
  private val context: Context
) {

  private val map = mutableMapOf<Int, CalendarWidgetPrefsProvider>()

  fun findOrCreate(widgetId: Int): CalendarWidgetPrefsProvider {
    Logger.d(TAG, "findOrCreate: widgetId = $widgetId")

    return map.getOrPut(widgetId) {
      Logger.d(TAG, "findOrCreate: create for widgetId = $widgetId")
      CalendarWidgetPrefsProvider(context, widgetId)
    }
  }

  companion object {
    private const val TAG = "WidgetPrefsHolder"
  }
}
