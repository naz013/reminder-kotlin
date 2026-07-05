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
  val backgroundColor: Int = -1,
  val opacity: Int = 100,
  val isArchived: Boolean = false,
  val isLoading: Boolean = false,
  val activeDialog: PreviewNoteDialog? = null,
  val showAdsBanner: Boolean = false,
)

/** Which modal dialog (if any) is currently shown above [PreviewNoteScreen]. */
enum class PreviewNoteDialog { DELETE, }

/**
 * Colors derived from [PreviewNoteState]'s background/opacity — computed once by
 * [PreviewNoteViewModel.colorsFor] so the Fragment/Compose layer never has to know about
 * [com.github.naz013.ui.common.theme.ThemeProvider] or the contrast math itself.
 */
data class NotePreviewColors(
  val background: Color,
  val statusBarColor: Int,
  val content: Color,
)
