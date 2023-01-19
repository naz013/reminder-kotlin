package com.elementary.tasks.core.app_widgets.events

import android.content.Context
import com.elementary.tasks.core.app_widgets.WidgetPrefsProvider

class EventsWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "new_events_pref", widgetId) {

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

  fun setTextSize(value: Float) {
    putFloat(WIDGET_TEXT_SIZE, value)
  }

  fun getTextSize(): Float {
    return getFloat(WIDGET_TEXT_SIZE)
  }

  companion object {
    private const val WIDGET_HEADER_BG = "new_events_header_bg"
    private const val WIDGET_ITEM_BG = "new_events_item_bg"
    private const val WIDGET_TEXT_SIZE = "new_events_text_size"
  }
}
