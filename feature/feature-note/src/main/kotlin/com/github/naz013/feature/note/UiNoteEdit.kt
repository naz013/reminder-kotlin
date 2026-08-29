package com.github.naz013.feature.note

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.ui.note.UiNoteImage

internal data class UiNoteEdit(
  val id: String,
  val document: NoteDocument,
  val typeface: Int,
  val images: List<UiNoteImage>,
  val colorIndex: Int,
  val opacity: Int,
  val fontSize: Int,
  val isArchived: Boolean,
)
