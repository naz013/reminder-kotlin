package com.github.naz013.ui.note

import androidx.compose.ui.graphics.Color
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.ui.common.selection.Selectable

data class UiNoteListItem(
  override val id: String,
  val content: NoteDocument,
  val backgroundColor: Color,
  val textColor: Color,
  val fontStyle: Int,
  val fontSize: Float,
  val images: List<UiNoteImage>,
  val isPinned: Boolean = false,
  override val isSelected: Boolean = false,
) : Selectable<UiNoteListItem> {
  override fun withSelected(selected: Boolean) = copy(isSelected = selected)
}
