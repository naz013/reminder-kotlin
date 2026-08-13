package com.github.naz013.ui.note

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.common.selection.Selectable

data class UiNoteListItem(
  override val id: String,
  val title: String,
  val text: String,
  val backgroundColor: Color,
  val textColor: Color,
  val fontStyle: Int,
  val fontSize: Float,
  val titleFontStyle: Int,
  val titleFontSize: Float,
  val images: List<UiNoteImage>,
  override val isSelected: Boolean = false,
) : Selectable<UiNoteListItem> {
  override fun withSelected(selected: Boolean) = copy(isSelected = selected)
}
