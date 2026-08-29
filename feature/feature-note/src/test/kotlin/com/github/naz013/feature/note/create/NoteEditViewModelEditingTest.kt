package com.github.naz013.feature.note.create

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.note.R
import io.mockk.coEvery
import io.mockk.every
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers the simple field/selection-editing surface of [NoteEditViewModel]: text and title field
 * changes, color/opacity/font selection, tab expansion, reminder-attached toggle, speech
 * recognition callbacks, and the date/time picker click events.
 */
internal class NoteEditViewModelEditingTest : NoteEditViewModelTestSupport() {

  @Test
  fun `onTextFieldValueChange updates the body text`() {
    val viewModel = buildViewModel()

    viewModel.onTextFieldValueChange(TextFieldValue("edited"))

    val state = viewModel.state.value
    assertEquals("edited", state.textFieldValue.text)
  }

  @Test
  fun `onFontSizeChanged updates the whole-note default size when nothing is selected`() {
    val viewModel = buildViewModel()

    viewModel.onFontSizeChanged(28)

    assertEquals(28, viewModel.state.value.fontSize)
    verify { notePreferences.lastNoteFontSize = 28 }
  }

  @Test
  fun `onFontSizeChanged applies a span to the current selection instead`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("Hello world", selection = TextRange(0, 5)))

    viewModel.onFontSizeChanged(28)

    assertEquals(
      listOf(NoteTextSpan(0, 5, NoteSpanAttribute.FontSize(28))),
      viewModel.state.value.spans,
    )
    verify(exactly = 0) { notePreferences.lastNoteFontSize = any() }
  }

  @Test
  fun `onFontStyleChanged updates the whole-note default style when nothing is selected`() {
    val viewModel = buildViewModel()

    viewModel.onFontStyleChanged(7)

    assertEquals(7, viewModel.state.value.fontStyle)
    verify { notePreferences.lastNoteFontStyle = 7 }
  }

  @Test
  fun `onToggleBold applies bold to the current selection`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("Hello world", selection = TextRange(0, 5)))

    viewModel.onToggleBold()

    assertEquals(listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Bold)), viewModel.state.value.spans)
  }

  @Test
  fun `onToggleBold removes bold when the whole selection is already bold`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("Hello world", selection = TextRange(0, 5)))
    viewModel.onToggleBold()

    viewModel.onToggleBold()

    assertEquals(emptyList<NoteTextSpan>(), viewModel.state.value.spans)
  }

  @Test
  fun `onApplyLineFormat sets a heading on the current line`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("Shopping\nMilk"))

    viewModel.onApplyLineFormat(NoteSpanAttribute.Heading1)

    assertEquals(
      listOf(NoteTextSpan(0, "Shopping".length, NoteSpanAttribute.Heading1)),
      viewModel.state.value.spans,
    )
  }

  @Test
  fun `onColorSelected updates the color index and recomputed colors`() {
    val viewModel = buildViewModel()
    every { notePreferences.isNoteColorRememberingEnabled } returns true

    viewModel.onColorSelected(5)

    assertEquals(5, viewModel.state.value.colorIndex)
    verify { notePreferences.lastNoteColor = 5 }
  }

  @Test
  fun `onColorSelected does not persist the choice when remembering is disabled`() {
    val viewModel = buildViewModel()
    every { notePreferences.isNoteColorRememberingEnabled } returns false

    viewModel.onColorSelected(5)

    assertEquals(5, viewModel.state.value.colorIndex)
    verify(exactly = 0) { notePreferences.lastNoteColor = any() }
  }

  @Test
  fun `onOpacityChanged updates opacity and persists it unconditionally`() {
    val viewModel = buildViewModel()

    viewModel.onOpacityChanged(40)

    assertEquals(40, viewModel.state.value.opacity)
    verify { notePreferences.noteColorOpacity = 40 }
  }

  @Test
  fun `onTabClicked expands a collapsed tab`() {
    val viewModel = buildViewModel()

    viewModel.onTabClicked(EditTab.COLOR)

    assertEquals(EditTab.COLOR, viewModel.state.value.expandedTab)
  }

  @Test
  fun `onTabClicked collapses the same tab when clicked again`() {
    val viewModel = buildViewModel()
    viewModel.onTabClicked(EditTab.TEXT_FORMAT)

    viewModel.onTabClicked(EditTab.TEXT_FORMAT)

    assertNull(viewModel.state.value.expandedTab)
  }

  @Test
  fun `onTabClicked switches to a different tab`() {
    val viewModel = buildViewModel()
    viewModel.onTabClicked(EditTab.TEXT_FORMAT)

    viewModel.onTabClicked(EditTab.IMAGE)

    assertEquals(EditTab.IMAGE, viewModel.state.value.expandedTab)
  }

  @Test
  fun `collapseExpandedTab collapses an expanded tab and reports it was expanded`() {
    val viewModel = buildViewModel()
    viewModel.onTabClicked(EditTab.REMINDER)

    val wasExpanded = viewModel.collapseExpandedTab()

    assertEquals(true, wasExpanded)
    assertNull(viewModel.state.value.expandedTab)
  }

  @Test
  fun `collapseExpandedTab reports false when no tab is expanded`() {
    val viewModel = buildViewModel()

    val wasExpanded = viewModel.collapseExpandedTab()

    assertEquals(false, wasExpanded)
  }

  @Test
  fun `onReminderAttachedChanged toggles the reminder attached flag`() {
    val viewModel = buildViewModel()

    viewModel.onReminderAttachedChanged(true)

    assertEquals(true, viewModel.state.value.isReminderAttached)
  }

  @Test
  fun `onDeleteRequested opens the delete confirmation dialog`() {
    val viewModel = buildViewModel()

    viewModel.onDeleteRequested()

    assertEquals(NoteEditDialog.DELETE, viewModel.state.value.activeDialog)
  }

  @Test
  fun `onDialogDismissed clears the active dialog`() {
    val viewModel = buildViewModel()
    viewModel.onDeleteRequested()

    viewModel.onDialogDismissed()

    assertNull(viewModel.state.value.activeDialog)
  }

  @Test
  fun `onDateClicked emits a ShowDatePicker event with the current date`() {
    every { textProvider.getString(R.string.select_date) } returns "Select date"
    val viewModel = buildViewModel()

    viewModel.onDateClicked()

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(
      NoteEditViewModel.ViewModelEvent.ShowDatePicker(viewModel.state.value.date, "Select date"),
      event,
    )
  }

  @Test
  fun `onTimeClicked emits a ShowTimePicker event with the current time`() {
    every { textProvider.getString(R.string.select_time) } returns "Select time"
    val viewModel = buildViewModel()

    viewModel.onTimeClicked()

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(
      NoteEditViewModel.ViewModelEvent.ShowTimePicker(viewModel.state.value.time, "Select time"),
      event,
    )
  }

  @Test
  fun `onSpeechStarted sets the speech state to STARTED`() {
    val viewModel = buildViewModel()

    viewModel.onSpeechStarted()

    assertEquals(SpeechUiState.STARTED, viewModel.state.value.speechState)
  }

  @Test
  fun `onSpeechSpeaking sets the speech state to SPEAKING`() {
    val viewModel = buildViewModel()

    viewModel.onSpeechSpeaking()

    assertEquals(SpeechUiState.SPEAKING, viewModel.state.value.speechState)
  }

  @Test
  fun `onSpeechStopped resets the speech state to IDLE`() {
    val viewModel = buildViewModel()
    viewModel.onSpeechStarted()

    viewModel.onSpeechStopped()

    assertEquals(SpeechUiState.IDLE, viewModel.state.value.speechState)
  }

  @Test
  fun `onSpeechError resets the speech state to IDLE`() {
    val viewModel = buildViewModel()
    viewModel.onSpeechStarted()

    viewModel.onSpeechError()

    assertEquals(SpeechUiState.IDLE, viewModel.state.value.speechState)
  }

  @Test
  fun `onSpeechResult replaces the text and moves the cursor to the end`() {
    val viewModel = buildViewModel()

    viewModel.onSpeechResult("hello world")

    val state = viewModel.state.value
    assertEquals(SpeechUiState.STOPPED, state.speechState)
    assertEquals("hello world", state.textFieldValue.text)
    assertEquals(TextRange("hello world".length), state.textFieldValue.selection)
  }

  @Test
  fun `shouldConfirmBeforeSaving is true only when imported from file and a duplicate exists in db`() {
    val noteWithImages = NoteWithImages(note = Note(key = "dup", syncState = SyncState.Synced))
    every { intentDataReader.get(IntentKeys.INTENT_ITEM, NoteWithImages::class.java) } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "dup")
    coEvery { noteRepository.getById("dup") } returns noteWithImages

    val viewModel = buildViewModel(fromIntentData = true)

    assertEquals(true, viewModel.shouldConfirmBeforeSaving())
  }

  @Test
  fun `shouldConfirmBeforeSaving is false for a brand-new note`() {
    val viewModel = buildViewModel()

    assertEquals(false, viewModel.shouldConfirmBeforeSaving())
  }
}
