package com.elementary.tasks.core.appwidgets

import android.annotation.SuppressLint
import android.content.Context

abstract class WidgetPrefsProvider(
  context: Context,
  fileName: String,
  private val widgetId: Int
) {

  private val sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

  @SuppressLint("ApplySharedPref")
  fun putFloat(key: String, value: Float) {
    sp.edit().putFloat(key + widgetId, value).commit()
  }

  fun getFloat(key: String, def: Float = 0f): Float {
    return sp.getFloat(key + widgetId, def)
  }

  @SuppressLint("ApplySharedPref")
  fun putInt(key: String, value: Int) {
    sp.edit().putInt(key + widgetId, value).commit()
  }

  fun getInt(key: String, def: Int = 0): Int {
    return sp.getInt(key + widgetId, def)
  }

  fun putString(key: String, value: String) {
    sp.edit().putString(key + widgetId, value).apply()
  }

  fun getString(key: String, def: String? = null): String? {
    return sp.getString(key + widgetId, def)
  }

  fun clear() {
    val edit = sp.edit()
    getKeys().forEach { key ->
      edit.remove(key + widgetId)
    }
    edit.apply()
  }

  open fun getKeys(): List<String> {
    return emptyList()
  }
}
