package com.elementary.tasks.core.appwidgets

import android.content.Context
import com.elementary.tasks.core.appwidgets.calendar.CalendarWidgetPrefsProvider
import com.github.naz013.logging.Logger

class WidgetPrefsHolder(
  private val context: Context
) {

  private val map = mutableMapOf<Key, WidgetPrefsProvider>()

  @Suppress("UNCHECKED_CAST")
  fun <T : WidgetPrefsProvider> findOrCreate(widgetId: Int, clazz: Class<T>): T {
    val key = Key(widgetId, clazz)

    Logger.d("findOrCreate: key = $key")

    return if (map.containsKey(key)) {
      Logger.d("findOrCreate: has key = $key")
      map[key]!! as T
    } else {
      val provider = when (clazz) {
        CalendarWidgetPrefsProvider::class.java -> {
          CalendarWidgetPrefsProvider(context, widgetId)
        }
        else -> {
          CalendarWidgetPrefsProvider(context, widgetId)
        }
      }
      Logger.d("findOrCreate: create for key = $key, provider = $provider")
      map[key] = provider
      provider as T
    }
  }

  data class Key(
    val widgetId: Int,
    val clazz: Class<*>
  )
}
