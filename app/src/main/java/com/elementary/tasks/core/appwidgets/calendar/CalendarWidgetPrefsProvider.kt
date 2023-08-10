package com.elementary.tasks.core.appwidgets.calendar

import android.content.Context
import com.elementary.tasks.core.appwidgets.WidgetPrefsProvider

class CalendarWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "new_calendar_pref", widgetId) {

  fun setMonth(value: Int) {
    putInt(CALENDAR_WIDGET_MONTH, value)
  }

  fun getMonth(): Int {
    return getInt(CALENDAR_WIDGET_MONTH, 0)
  }

  fun setYear(value: Int) {
    putInt(CALENDAR_WIDGET_YEAR, value)
  }

  fun getYear(): Int {
    return getInt(CALENDAR_WIDGET_YEAR, 0)
  }

  fun setHeaderBackground(value: Int) {
    putInt(WIDGET_HEADER_BG_COLOR, value)
  }

  fun getHeaderBackground(): Int {
    return getInt(WIDGET_HEADER_BG_COLOR, 0)
  }

  fun setBackground(value: Int) {
    putInt(WIDGET_BG, value)
  }

  fun getBackground(): Int {
    return getInt(WIDGET_BG, 0)
  }

  fun getRowHeightDp(): Float {
    return getFloat(ROW_HEIGHT)
  }

  fun setRowHeight(value: Float) {
    putFloat(ROW_HEIGHT, value)
  }

  companion object {
    private const val WIDGET_HEADER_BG_COLOR = "new_calendar_header_bg"
    private const val WIDGET_BG = "new_calendar_bg"
    private const val CALENDAR_WIDGET_MONTH = "new_calendar_month_"
    private const val CALENDAR_WIDGET_YEAR = "new_calendar_year_"
    private const val ROW_HEIGHT = "new_calendar_row_height"
  }
}
