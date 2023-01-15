package com.elementary.tasks.core.app_widgets

import android.content.Context

abstract class WidgetPrefsProvider(
  context: Context,
  fileName: String,
  private val widgetId: Int
) {

  private val sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

  fun putInt(key: String, value: Int) {
    sp.edit().putInt(key + widgetId, value).apply()
  }

  fun getInt(key: String, def: Int = 0): Int {
    return sp.getInt(key + widgetId, def)
  }
}
