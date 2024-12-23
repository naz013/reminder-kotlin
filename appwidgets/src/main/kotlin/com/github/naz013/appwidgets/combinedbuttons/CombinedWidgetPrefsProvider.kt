package com.github.naz013.appwidgets.combinedbuttons

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class CombinedWidgetPrefsProvider(
  context: Context,
  val widgetId: Int
) : WidgetPrefsProvider(context, "combined_buttons_prefs", widgetId) {

  fun setWidgetBackground(value: Int) {
    putInt(WIDGET_BG_COLOR, value)
  }

  fun getWidgetBackground(): Int {
    return getInt(WIDGET_BG_COLOR, 0)
  }

  companion object {
    private const val WIDGET_BG_COLOR = "widget_bg_color"
  }
}
