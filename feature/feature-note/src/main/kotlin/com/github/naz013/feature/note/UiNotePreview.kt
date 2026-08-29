package com.github.naz013.feature.note

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.ui.note.UiNoteImage

internal data class UiNotePreview(
  val id: String,
  val document: NoteDocument,
  val fontStyle: Int,
  val fontSize: Float,
  val images: List<UiNoteImage>,
  val uniqueId: Int,
  val isArchived: Boolean,
  val isPinned: Boolean,
)
