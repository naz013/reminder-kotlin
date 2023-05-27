package com.elementary.tasks.core.app_widgets

import android.content.Context
import com.elementary.tasks.core.app_widgets.calendar.CalendarWidgetPrefsProvider
import timber.log.Timber

class WidgetPrefsHolder(
  private val context: Context
) {

  private val map = mutableMapOf<Key, WidgetPrefsProvider>()

  @Suppress("UNCHECKED_CAST")
  fun <T : WidgetPrefsProvider> findOrCreate(widgetId: Int, clazz: Class<T>): T {
    val key = Key(widgetId, clazz)

    Timber.d("findOrCreate: key = $key")

    return if (map.containsKey(key)) {
      Timber.d("findOrCreate: has key = $key")
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
      Timber.d("findOrCreate: create for key = $key, provider = $provider")
      map[key] = provider
      provider as T
    }
  }

  data class Key(
    val widgetId: Int,
    val clazz: Class<*>
  )
}
