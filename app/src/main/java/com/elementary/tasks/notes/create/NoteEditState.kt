package com.elementary.tasks.notes.create

import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.github.naz013.domain.font.FontParams

data class NoteEditState(
  val colorIndex: Int = 0,
  val opacity: Int = 100,
  val palette: Int = 0,
  val fontStyle: Int = FontParams.DEFAULT_FONT_STYLE,
  val fontSize: Int = FontParams.DEFAULT_FONT_SIZE,
  val titleFontStyle: Int = FontParams.DEFAULT_FONT_STYLE,
  val titleFontSize: Int = FontParams.DEFAULT_TITLE_FONT_SIZE,
  val focusedField: NoteTextField = NoteTextField.BODY,
  val images: List<UiNoteImage> = emptyList(),
  val isReminderAttached: Boolean = false,
  val reminderDateFormatted: String = "",
  val reminderTimeFormatted: String = "",
  val expandedTab: EditTab? = null,
  val isNoteEdited: Boolean = false,
  val isFromFile: Boolean = false,
)

enum class EditTab { COLOR, FONT, REMINDER, IMAGE }

/** Which of the two text fields last had focus — drives which field's font size/style
 *  [com.elementary.tasks.notes.create.FontPanel] displays and edits. */
enum class NoteTextField { TITLE, BODY }

/**
 * Colors derived from [NoteEditState]'s color/opacity/palette — computed once by
 * [CreateNoteViewModel.colorsFor] so the Activity/Compose layer never has to know about
 * [com.github.naz013.ui.common.theme.ThemeProvider] or the contrast math itself.
 */
data class NoteColors(
  val background: Int,
  val statusBarColor: Int,
  val content: Int,
  val sliderColors: IntArray
)

/** Which modal dialog (if any) is currently shown above [NoteEditScreen]. */
enum class NoteEditDialog { DELETE, SAME_NOTE }

/**
 * Owned by the Activity (not this state), since [com.elementary.tasks.core.speech.SpeechEngine]
 * is tied to Activity lifecycle/context. Kept here only as the shared vocabulary for the mic
 * button's visual state.
 */
enum class SpeechUiState { IDLE, STARTED, SPEAKING, STOPPED }

/**
 * A one-shot instruction to programmatically replace the editor's text (initial load, speech
 * recognition result, drag-drop parsed text, share-intent text). [boldRange] mirrors the
 * newly-recognized portion of a speech result so the UI can render it in bold, matching the
 * previous [com.github.naz013.ui.common.view.gradient.UiGradientEditText] bold-section behavior.
 */
data class TextUpdate(
  val text: String,
  val boldRange: IntRange? = null
)
