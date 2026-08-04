package com.github.naz013.appwidgets.notes.data

import androidx.compose.ui.graphics.Color

internal data class NotesAppWidgetState(
  val widgetId: Int,
  val headerBackgroundColor: Int,
  val headerContrastColor: Color,
  val items: List<UiNoteWidgetItem>
)
