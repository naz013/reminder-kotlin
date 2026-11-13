package com.github.naz013.appwidgets.habit

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class HabitWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "habit_widget_prefs", widgetId) {

  fun setBackground(value: Int) {
    putInt(WIDGET_BG_COLOR, value)
  }

  fun getBackground(): Int {
    return getInt(WIDGET_BG_COLOR, 0)
  }

  override fun getKeys(): List<String> {
    return listOf(WIDGET_BG_COLOR)
  }

  companion object {
    private const val WIDGET_BG_COLOR = "widget_habit_bg_color"
  }
}
