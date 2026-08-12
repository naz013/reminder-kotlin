package com.github.naz013.appwidgets.birthdays

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class BirthdaysWidgetPrefsProvider(
  context: Context,
  val widgetId: Int
) : WidgetPrefsProvider(context, "widget_birthdays_pref", widgetId) {

  fun setWidgetBackground(value: Int) {
    putInt(WIDGET_BG, value)
  }

  fun getWidgetBackground(): Int {
    return getInt(WIDGET_BG, 2)
  }

  companion object {
    private const val WIDGET_BG = "widget_birthdays_header_bg"
  }
}
