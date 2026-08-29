package com.github.naz013.feature.note.preview

import androidx.compose.ui.graphics.Color
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.feature.note.preview.reminders.UiNoteAttachedReminder
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.tag.TagChipState

internal data class PreviewNoteState(
  val id: String = "",
  val document: NoteDocument = NoteDocument(),
  val fontStyle: Int = FontParams.DEFAULT_FONT_STYLE,
  val fontSize: Float = FontParams.DEFAULT_FONT_SIZE.toFloat(),
  val images: List<UiNoteImage> = emptyList(),
  val tags: List<TagChipState> = emptyList(),
  val reminders: List<UiNoteAttachedReminder> = emptyList(),
  val isArchived: Boolean = false,
  val isPinned: Boolean = false,
  val isLoading: Boolean = false,
  val background: Color = Color.Transparent,
  val content: Color = Color.Unspecified,
)
