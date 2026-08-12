package com.github.naz013.feature.note.preview

import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import com.github.naz013.feature.note.preview.reminders.UiNoteAttachedReminder
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.tag.TagChipState

internal data class PreviewNoteState(
  val id: String = "",
  val title: String = "",
  val text: String = "",
  val titleTypeface: Typeface? = null,
  val typeface: Typeface? = null,
  val titleTextSize: Float = 20f,
  val textSize: Float = 18f,
  val images: List<UiNoteImage> = emptyList(),
  val tags: List<TagChipState> = emptyList(),
  val reminders: List<UiNoteAttachedReminder> = emptyList(),
  val isArchived: Boolean = false,
  val isLoading: Boolean = false,
  val background: Color = Color.Transparent,
  val content: Color = Color.Unspecified,
)
