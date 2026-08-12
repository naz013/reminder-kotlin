package com.github.naz013.appwidgets.notes.data

internal data class NotesAppWidgetState(
  val widgetId: Int,
  val backgroundColor: Int,
  val items: List<UiNoteWidgetItem>
)
