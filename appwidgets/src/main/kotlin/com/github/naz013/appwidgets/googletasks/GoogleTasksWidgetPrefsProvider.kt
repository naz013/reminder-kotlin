package com.github.naz013.appwidgets.googletasks

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class GoogleTasksWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "new_tasks_pref", widgetId) {

  fun setBackground(value: Int) {
    putInt(WIDGET_HEADER_BG, value)
  }

  fun getBackground(): Int {
    return getInt(WIDGET_HEADER_BG)
  }

  companion object {
    private const val WIDGET_HEADER_BG = "new_tasks_header_bg"
  }
}
