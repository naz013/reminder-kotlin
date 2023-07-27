package com.elementary.tasks.core.app_widgets.singlenote

import android.content.Context
import com.elementary.tasks.core.app_widgets.WidgetPrefsProvider
import com.elementary.tasks.core.views.drawable.NoteDrawableParams

class SingleNoteWidgetPrefsProvider(
  context: Context,
  internal val widgetId: Int
) : WidgetPrefsProvider(context, "single_note_widget_prefs", widgetId) {

  fun setNoteId(id: String) {
    putString(WIDGET_NOTE_ID, id)
  }

  fun getNoteId(): String? {
    return getString(WIDGET_NOTE_ID)
  }

  fun setTextSize(textSize: Float) {
    putFloat(WIDGET_TEXT_SIZE, textSize)
  }

  fun getTextSize(): Float {
    return getFloat(WIDGET_TEXT_SIZE, def = 16f)
  }

  fun setHorizontalAlignment(horizontalAlignment: NoteDrawableParams.HorizontalAlignment) {
    putString(WIDGET_TEXT_HOR_ALIGNMENT, horizontalAlignment.name)
  }

  fun getHorizontalAlignment(): NoteDrawableParams.HorizontalAlignment {
    return getString(
      key = WIDGET_TEXT_HOR_ALIGNMENT,
      def = NoteDrawableParams.HorizontalAlignment.CENTER.name
    )?.let { runCatching { NoteDrawableParams.HorizontalAlignment.valueOf(it) }.getOrNull() }
      ?: NoteDrawableParams.HorizontalAlignment.CENTER
  }

  fun setVerticalAlignment(verticalAlignment: NoteDrawableParams.VerticalAlignment) {
    putString(WIDGET_TEXT_VER_ALIGNMENT, verticalAlignment.name)
  }

  fun getVerticalAlignment(): NoteDrawableParams.VerticalAlignment {
    return getString(
      key = WIDGET_TEXT_VER_ALIGNMENT,
      def = NoteDrawableParams.VerticalAlignment.CENTER.name
    )?.let { runCatching { NoteDrawableParams.VerticalAlignment.valueOf(it) }.getOrNull() }
      ?: NoteDrawableParams.VerticalAlignment.CENTER
  }

  override fun getKeys(): List<String> {
    return listOf(
      WIDGET_NOTE_ID,
      WIDGET_TEXT_SIZE,
      WIDGET_TEXT_HOR_ALIGNMENT,
      WIDGET_TEXT_VER_ALIGNMENT
    )
  }

  companion object {
    private const val WIDGET_NOTE_ID = "widget_note_id"
    private const val WIDGET_TEXT_SIZE = "widget_text_size"
    private const val WIDGET_TEXT_VER_ALIGNMENT = "widget_text_hor_alignment"
    private const val WIDGET_TEXT_HOR_ALIGNMENT = "widget_text_ver_alignment"
  }
}
