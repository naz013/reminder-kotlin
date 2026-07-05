package com.elementary.tasks.notes.create

import androidx.compose.ui.text.input.TextFieldValue
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
  val textFieldValue: TextFieldValue = TextFieldValue(),
  val titleFieldValue: TextFieldValue = TextFieldValue(),
  val boldRange: IntRange? = null,
  val speechState: SpeechUiState = SpeechUiState.IDLE,
  val activeDialog: NoteEditDialog? = null,
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
  val sliderColors: IntArray,
)

/** Which modal dialog (if any) is currently shown above [NoteEditScreen]. */
enum class NoteEditDialog { DELETE, SAME_NOTE }

enum class SpeechUiState { IDLE, STARTED, SPEAKING, STOPPED }

/**
 * One-shot signal that [NoteEditState.textFieldValue] was replaced programmatically (initial
 * load, speech recognition result, drag-drop parsed text, share-intent text) rather than by the
 * user typing — the [com.elementary.tasks.core.speech.SpeechEngine] instance is Context-bound and
 * lives outside the ViewModel, so it needs telling to resync its own text buffer to [text].
 */
data class TextUpdate(val text: String)
