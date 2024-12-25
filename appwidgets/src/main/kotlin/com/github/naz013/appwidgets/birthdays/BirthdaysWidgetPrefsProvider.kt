package com.github.naz013.appwidgets.birthdays

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class BirthdaysWidgetPrefsProvider(
  context: Context,
  val widgetId: Int
) : WidgetPrefsProvider(context, "widget_birthdays_pref", widgetId) {

  fun setHeaderBackground(value: Int) {
    putInt(WIDGET_HEADER_BG, value)
  }

  fun getHeaderBackground(): Int {
    return getInt(WIDGET_HEADER_BG)
  }

  fun setItemBackground(value: Int) {
    putInt(WIDGET_ITEM_BG, value)
  }

  fun getItemBackground(): Int {
    return getInt(WIDGET_ITEM_BG)
  }

  companion object {
    private const val WIDGET_HEADER_BG = "widget_birthdays_header_bg"
    private const val WIDGET_ITEM_BG = "widget_birthdays_item_bg"
  }
}
