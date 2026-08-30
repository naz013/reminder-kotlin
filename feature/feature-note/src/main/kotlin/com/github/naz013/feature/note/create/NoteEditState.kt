package com.github.naz013.feature.note.create

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.ui.note.NoteColorEngine
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.tag.TagChipState
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.UUID

internal data class NoteEditState(
  val colorIndex: Int = 0,
  val opacity: Int = 100,
  val noteColors: NoteColorEngine.Colors = NoteColorEngine.Colors(Color.Unspecified, Color.Unspecified),
  val fontStyle: Int = FontParams.DEFAULT_FONT_STYLE,
  val fontSize: Int = FontParams.DEFAULT_FONT_SIZE,
  val textFieldValue: TextFieldValue = TextFieldValue(),
  val spans: List<NoteTextSpan> = emptyList(),
  val images: List<UiNoteImage> = emptyList(),
  val speechState: SpeechUiState = SpeechUiState.IDLE,
  val date: LocalDate = LocalDate.now(),
  val time: LocalTime = LocalTime.now(),
  val reminderDateFormatted: String = "",
  val reminderTimeFormatted: String = "",
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
) {
  /** What the floating bar's format/color/gradient controls should show right now - the format
   * at the selection (or, for a collapsed cursor, at the character just before it), so
   * continuing to type or re-selecting elsewhere keeps the toolbar honest. */
  val activeFormat: ActiveTextFormat
    get() {
      val text = textFieldValue.text
      val selection = textFieldValue.selection
      val start = selection.min
      val end = selection.max
      return ActiveTextFormat(
        bold = isAttributeActiveOverRange(spans, NoteSpanAttribute.Bold, start, end),
        italic = isAttributeActiveOverRange(spans, NoteSpanAttribute.Italic, start, end),
        underline = isAttributeActiveOverRange(spans, NoteSpanAttribute.Underline, start, end),
        strikethrough = isAttributeActiveOverRange(spans, NoteSpanAttribute.Strikethrough, start, end),
        lineFormat = activeLineFormat(text, spans, start),
        solidColorArgb = activeSolidColorArgb(spans, start, end),
      )
    }
}

/** The inline/line format active at the current cursor or selection - see [NoteEditState.activeFormat]. */
internal data class ActiveTextFormat(
  val bold: Boolean = false,
  val italic: Boolean = false,
  val underline: Boolean = false,
  val strikethrough: Boolean = false,
  val lineFormat: NoteSpanAttribute? = null,
  val solidColorArgb: Int? = null,
)

internal enum class EditTab { COLOR, REMINDER, IMAGE, TAGS, TEXT_FORMAT, TEXT_COLOR }

/** Which modal dialog (if any) is currently shown above [NoteEditScreen]. */
internal enum class NoteEditDialog { DELETE, SAME_NOTE }

internal enum class SpeechUiState { IDLE, STARTED, SPEAKING, STOPPED }

/**
 * One-shot signal that [NoteEditState.textFieldValue] was replaced programmatically (initial
 * load, speech recognition result, drag-drop parsed text, share-intent text) rather than by the
 * user typing — the [com.github.naz013.common.speech.SpeechEngine] instance is Context-bound and
 * lives outside the ViewModel, so it needs telling to resync its own text buffer to [text].
 */
internal data class TextUpdate(
  val text: String,
)
