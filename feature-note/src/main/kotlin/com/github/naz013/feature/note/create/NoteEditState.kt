package com.github.naz013.feature.note.create

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.note.NoteColorEngine
import com.github.naz013.domain.font.FontParams
import com.github.naz013.ui.tag.TagChipState
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.UUID

data class NoteEditState(
  val colorIndex: Int = 0,
  val opacity: Int = 100,
  val noteColors: NoteColorEngine.Colors = NoteColorEngine.Colors(Color.Unspecified, Color.Unspecified),
  val fontStyle: Int = FontParams.DEFAULT_FONT_STYLE,
  val fontSize: Int = FontParams.DEFAULT_FONT_SIZE,
  val titleFontStyle: Int = FontParams.DEFAULT_FONT_STYLE,
  val titleFontSize: Int = FontParams.DEFAULT_TITLE_FONT_SIZE,
  val textFieldValue: TextFieldValue = TextFieldValue(),
  val titleFieldValue: TextFieldValue = TextFieldValue(),
  val boldRange: IntRange? = null,
  val images: List<UiNoteImage> = emptyList(),
  val speechState: SpeechUiState = SpeechUiState.IDLE,
  val date: LocalDate = LocalDate.now(),
  val time: LocalTime = LocalTime.now(),
  val reminderDateFormatted: String = "",
  val reminderTimeFormatted: String = "",
  val focusedField: NoteTextField = NoteTextField.BODY,
  val expandedTab: EditTab? = null,
  val activeDialog: NoteEditDialog? = null,
  val hasCamera: Boolean = false,
  val hasSameInDb: Boolean = false,
  val isFromFile: Boolean = false,
  val isReminderAttached: Boolean = false,
  val canDelete: Boolean = false,
  val reminderId: String? = null,
  val noteId: String = UUID.randomUUID().toString(),
  val sliderColors: List<Color> = emptyList(),
  val hapticFeedbackEnabled: Boolean = true,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
)

enum class EditTab { COLOR, FONT, REMINDER, IMAGE, TAGS }

/** Which of the two text fields last had focus — drives which field's font size/style
 *  [com.elementary.tasks.notes.create.FontPanel] displays and edits. */
enum class NoteTextField { TITLE, BODY }

/** Which modal dialog (if any) is currently shown above [NoteEditScreen]. */
enum class NoteEditDialog { DELETE, SAME_NOTE }

enum class SpeechUiState { IDLE, STARTED, SPEAKING, STOPPED }

/**
 * One-shot signal that [NoteEditState.textFieldValue] was replaced programmatically (initial
 * load, speech recognition result, drag-drop parsed text, share-intent text) rather than by the
 * user typing — the [com.github.naz013.common.speech.SpeechEngine] instance is Context-bound and
 * lives outside the ViewModel, so it needs telling to resync its own text buffer to [text].
 */
data class TextUpdate(
  val text: String,
)
