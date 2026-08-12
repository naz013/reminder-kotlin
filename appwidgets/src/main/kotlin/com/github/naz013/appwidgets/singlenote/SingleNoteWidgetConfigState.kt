package com.github.naz013.appwidgets.singlenote

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.github.naz013.appwidgets.singlenote.drawable.NoteDrawableParams
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.note.UiNoteListItem

internal data class SingleNoteWidgetConfigState(
  val notes: List<UiNoteListItem> = emptyList(),
  val selectedNoteId: String? = null,
  val textSize: Float = 16f,
  val horizontalAlignment: NoteDrawableParams.HorizontalAlignment = NoteDrawableParams.HorizontalAlignment.CENTER,
  val verticalAlignment: NoteDrawableParams.VerticalAlignment = NoteDrawableParams.VerticalAlignment.CENTER,
  val textColorIndex: Int = ThemeProvider.AppColorIndex.BLACK,
  val textColorOpacity: Float = 100f,
  val overlayColorIndex: Int = ThemeProvider.AppColorIndex.WHITE,
  val overlayColorOpacity: Float = 0f,
  val previewBitmap: Bitmap? = null,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
)

internal sealed interface SingleNoteWidgetConfigEvent {
  data object NoteNotSelected : SingleNoteWidgetConfigEvent
  data object Saved : SingleNoteWidgetConfigEvent
}
