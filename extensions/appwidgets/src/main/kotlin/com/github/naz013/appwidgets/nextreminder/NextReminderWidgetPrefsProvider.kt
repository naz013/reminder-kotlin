package com.github.naz013.appwidgets.nextreminder

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider
import com.github.naz013.appwidgets.WidgetUtils

internal class NextReminderWidgetPrefsProvider(
  context: Context,
  val widgetId: Int
) : WidgetPrefsProvider(context, "next_reminder_widget_prefs", widgetId) {

  fun setWidgetBackground(value: Int) {
    putInt(WIDGET_BG_COLOR, value)
  }

  fun getWidgetBackground(): Int {
    return getInt(WIDGET_BG_COLOR, WidgetUtils.DYNAMIC_COLOR_INDEX)
  }

  companion object {
    private const val WIDGET_BG_COLOR = "widget_bg_color"
  }
}
