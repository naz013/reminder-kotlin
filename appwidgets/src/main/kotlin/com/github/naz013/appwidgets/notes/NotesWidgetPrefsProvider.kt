package com.github.naz013.appwidgets.notes

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider

internal class NotesWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "new_notes_prefs", widgetId) {

  fun setHeaderBackground(value: Int) {
    putInt(WIDGET_HEADER_BG_COLOR, value)
  }

  fun getHeaderBackground(): Int {
    return getInt(WIDGET_HEADER_BG_COLOR, 0)
  }

  companion object {
    private const val WIDGET_HEADER_BG_COLOR = "widget_header_bg_color"
  }
}
