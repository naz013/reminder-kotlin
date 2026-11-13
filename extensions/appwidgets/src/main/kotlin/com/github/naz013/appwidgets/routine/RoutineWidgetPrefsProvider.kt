package com.github.naz013.appwidgets.routine

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class RoutineWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "routine_widget_prefs", widgetId) {

  fun setRoutineId(value: String?) {
    putString(WIDGET_ROUTINE_ID, value.orEmpty())
  }

  /** Null means the widget follows the next scheduled routine. */
  fun getRoutineId(): String? {
    return getString(WIDGET_ROUTINE_ID, null)?.takeIf { it.isNotEmpty() }
  }

  override fun getKeys(): List<String> {
    return listOf(WIDGET_ROUTINE_ID)
  }

  companion object {
    private const val WIDGET_ROUTINE_ID = "widget_routine_id"
  }
}
