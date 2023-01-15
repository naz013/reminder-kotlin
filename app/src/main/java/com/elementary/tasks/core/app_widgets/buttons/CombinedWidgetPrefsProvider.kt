package com.elementary.tasks.core.app_widgets.buttons

import android.content.Context
import com.elementary.tasks.core.app_widgets.WidgetPrefsProvider

class CombinedWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
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
