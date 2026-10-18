package com.github.naz013.appwidgets.todo

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class TodoWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "todo_widget_prefs", widgetId) {

  fun setReminderId(id: String) {
    putString(WIDGET_REMINDER_ID, id)
  }

  fun getReminderId(): String? {
    return getString(WIDGET_REMINDER_ID)
  }

  fun setBackground(value: Int) {
    putInt(WIDGET_BG_COLOR, value)
  }

  fun getBackground(): Int {
    return getInt(WIDGET_BG_COLOR, 0)
  }

  override fun getKeys(): List<String> {
    return listOf(WIDGET_REMINDER_ID, WIDGET_BG_COLOR)
  }

  companion object {
    private const val WIDGET_REMINDER_ID = "widget_todo_reminder_id"
    private const val WIDGET_BG_COLOR = "widget_todo_bg_color"
  }
}
