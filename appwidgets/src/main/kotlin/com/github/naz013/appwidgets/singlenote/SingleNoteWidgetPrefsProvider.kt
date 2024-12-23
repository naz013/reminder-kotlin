package com.github.naz013.appwidgets.singlenote

import android.content.Context
import com.github.naz013.appwidgets.WidgetPrefsProvider
import com.github.naz013.appwidgets.singlenote.drawable.NoteDrawableParams
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.theme.ThemeProvider

internal class SingleNoteWidgetPrefsProvider(
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

  fun setTextColorPosition(position: Int) {
    putInt(WIDGET_TEXT_COLOR_POSITION, position)
  }

  fun getTextColorPosition(): Int {
    return getInt(WIDGET_TEXT_COLOR_POSITION, def = ThemeProvider.Color.BLACK).also {
      Logger.d("getTextColorPosition: $it")
    }
  }

  fun setTextColorOpacity(opacity: Float) {
    putFloat(WIDGET_TEXT_COLOR_OPACITY, opacity)
  }

  fun getTextColorOpacity(): Float {
    return getFloat(WIDGET_TEXT_COLOR_OPACITY, def = 100f)
  }

  fun setOverlayColorPosition(position: Int) {
    putInt(WIDGET_OVERLAY_COLOR_POSITION, position)
  }

  fun getOverlayColorPosition(): Int {
    return getInt(WIDGET_OVERLAY_COLOR_POSITION, def = ThemeProvider.Color.WHITE).also {
      Logger.d("getOverlayColorPosition: $it")
    }
  }

  fun setOverlayColorOpacity(opacity: Float) {
    putFloat(WIDGET_OVERLAY_COLOR_OPACITY, opacity)
  }

  fun getOverlayColorOpacity(): Float {
    return getFloat(WIDGET_OVERLAY_COLOR_OPACITY, def = 0f)
  }

  override fun getKeys(): List<String> {
    return listOf(
      WIDGET_NOTE_ID,
      WIDGET_TEXT_SIZE,
      WIDGET_TEXT_HOR_ALIGNMENT,
      WIDGET_TEXT_VER_ALIGNMENT,
      WIDGET_TEXT_COLOR_POSITION,
      WIDGET_TEXT_COLOR_OPACITY,
      WIDGET_OVERLAY_COLOR_POSITION,
      WIDGET_OVERLAY_COLOR_OPACITY
    )
  }

  companion object {
    private const val WIDGET_NOTE_ID = "widget_note_id"
    private const val WIDGET_TEXT_SIZE = "widget_text_size"
    private const val WIDGET_TEXT_VER_ALIGNMENT = "widget_text_hor_alignment"
    private const val WIDGET_TEXT_HOR_ALIGNMENT = "widget_text_ver_alignment"

    private const val WIDGET_TEXT_COLOR_POSITION = "widget_text_color_position"
    private const val WIDGET_TEXT_COLOR_OPACITY = "widget_text_color_opacity"

    private const val WIDGET_OVERLAY_COLOR_POSITION = "widget_overlay_color_position"
    private const val WIDGET_OVERLAY_COLOR_OPACITY = "widget_overlay_color_opacity"
  }
}
