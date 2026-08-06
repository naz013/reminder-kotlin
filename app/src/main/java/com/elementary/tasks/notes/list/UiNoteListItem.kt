package com.elementary.tasks.notes.list

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.core.data.ui.note.UiNoteImage

data class UiNoteListItem(
  val id: String,
  val title: String,
  val text: String,
  val backgroundColor: Color,
  val textColor: Color,
  val fontStyle: Int,
  val fontSize: Float,
  val titleFontStyle: Int,
  val titleFontSize: Float,
  val images: List<UiNoteImage>,
)
