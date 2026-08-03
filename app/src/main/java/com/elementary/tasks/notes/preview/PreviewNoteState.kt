package com.elementary.tasks.notes.preview

import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.notes.preview.reminders.UiNoteAttachedReminder

data class PreviewNoteState(
  val id: String = "",
  val title: String = "",
  val text: String = "",
  val titleTypeface: Typeface? = null,
  val typeface: Typeface? = null,
  val titleTextSize: Float = 20f,
  val textSize: Float = 18f,
  val images: List<UiNoteImage> = emptyList(),
  val reminders: List<UiNoteAttachedReminder> = emptyList(),
  val isArchived: Boolean = false,
  val isLoading: Boolean = false,
  val background: Color = Color.Transparent,
  val content: Color = Color.Unspecified,
)
