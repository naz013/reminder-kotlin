package com.elementary.tasks.core.appwidgets.googletasks

import android.content.Context
import com.elementary.tasks.core.appwidgets.WidgetPrefsProvider

class GoogleTasksWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "new_tasks_pref", widgetId) {

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
    private const val WIDGET_HEADER_BG = "new_tasks_header_bg"
    private const val WIDGET_ITEM_BG = "new_tasks_item_bg"
  }
}
