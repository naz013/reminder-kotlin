package com.elementary.tasks.core.app_widgets.singlenote

import android.content.Context
import com.elementary.tasks.core.app_widgets.WidgetPrefsProvider

class SingleNoteWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "single_note_widget_prefs", widgetId) {

  fun setWidgetBackground(value: Int) {
    putInt(WIDGET_BG_COLOR, value)
  }

  fun getWidgetBackground(): Int {
    return getInt(WIDGET_BG_COLOR, 0)
  }

  fun setNoteId(id: String) {
    putString(WIDGET_NOTE_ID, id)
  }

  fun getNoteId(): String? {
    return getString(WIDGET_NOTE_ID)
  }

  companion object {
    private const val WIDGET_BG_COLOR = "widget_bg_color"
    private const val WIDGET_NOTE_ID = "widget_note_id"
  }
}
