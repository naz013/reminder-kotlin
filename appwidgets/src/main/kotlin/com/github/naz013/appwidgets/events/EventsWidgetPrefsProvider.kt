package com.github.naz013.appwidgets.events

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class EventsWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "new_events_pref", widgetId) {

  fun setBackground(value: Int) {
    putInt(WIDGET_HEADER_BG, value)
  }

  fun getBackground(): Int {
    return getInt(WIDGET_HEADER_BG)
  }

  fun setTextSize(value: Float) {
    putFloat(WIDGET_TEXT_SIZE, value)
  }

  fun getTextSize(): Float {
    return getFloat(WIDGET_TEXT_SIZE)
  }

  companion object {
    private const val WIDGET_HEADER_BG = "new_events_header_bg"
    private const val WIDGET_TEXT_SIZE = "new_events_text_size"
  }
}
