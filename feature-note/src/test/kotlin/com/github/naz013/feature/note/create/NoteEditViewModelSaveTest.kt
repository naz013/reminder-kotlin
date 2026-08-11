package com.github.naz013.feature.note.create

import androidx.compose.ui.text.input.TextFieldValue
import com.github.naz013.feature.note.R
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Covers [NoteEditViewModel.onSaveClicked] / `saveNote()`: the file-vs-db conflict dialog, plain
 * note persistence, and the reminder attach/detach/reuse/outdated branches inside `createReminder`
 * and `saveReminder`.
 */
class NoteEditViewModelSaveTest : NoteEditViewModelTestSupport() {

  @Test
  fun `onSaveClicked opens the same-note conflict dialog instead of saving when imported and a duplicate exists`() {
    val noteWithImages = NoteWithImages(note = Note(key = "dup", syncState = SyncState.Synced))
    every { intentDataReader.get(IntentKeys.INTENT_ITEM, NoteWithImages::class.java) } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "dup")
    coEvery { noteRepository.getById("dup") } returns noteWithImages
    val viewModel = buildViewModel(fromIntentData = true)

    viewModel.onSaveClicked()

    assertEquals(NoteEditDialog.SAME_NOTE, viewModel.state.value.activeDialog)
    coVerify(exactly = 0) { saveNoteUseCase(any()) }
  }

  @Test
  fun `onSaveClicked saves directly when there is no file-db conflict`() {
    val viewModel = buildViewModel()
    viewModel.onTitleFieldValueChange(TextFieldValue("Title"))

    viewModel.onSaveClicked()

    coVerify(exactly = 1) { saveNoteUseCase(any()) }
    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.MoveBack, event)
  }

  @Test
  fun `saveNote creates a new note without a reminder and updates the widgets`() {
    val viewModel = buildViewModel()
    viewModel.onTitleFieldValueChange(TextFieldValue("Groceries"))
    viewModel.onTextFieldValueChange(TextFieldValue("Milk, eggs"))

    viewModel.saveNote()

    coVerify(exactly = 1) { saveNoteUseCase(any()) }
    coVerify(exactly = 0) { activateReminderUseCase(any(), any()) }
    coVerify(exactly = 0) { deleteReminderUseCase(any()) }
    verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    verify(exactly = 1) { appWidgetUpdater.updateAllWidgets() }
    verify { analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_NOTE)) }
    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.MoveBack, event)
  }

  @Test
  fun `saveNote persists the trimmed title and body`() {
    val viewModel = buildViewModel()
    viewModel.onTitleFieldValueChange(TextFieldValue("  Groceries  "))
    viewModel.onTextFieldValueChange(TextFieldValue("  Milk  "))
    val saved = slot<NoteWithImages>()
    coEvery { saveNoteUseCase(capture(saved)) } returns Unit

    viewModel.saveNote()

    assertEquals("Groceries", saved.captured.note?.title)
    assertEquals("Milk", saved.captured.note?.summary)
  }

  @Test
  fun `saveNote creates and activates a new reminder when the reminder switch is on`() {
    val viewModel = buildViewModel()
    viewModel.onTitleFieldValueChange(TextFieldValue("Groceries"))
    viewModel.onReminderAttachedChanged(true)
    viewModel.onNewDate(LocalDate.of(2026, 8, 1))
    viewModel.onNewTime(LocalTime.of(9, 0))

    viewModel.saveNote()

    val reminderSlot = slot<ReminderV2>()
    coVerify(exactly = 1) { activateReminderUseCase(capture(reminderSlot), any()) }
    assertEquals(RecurrenceRule.Once, reminderSlot.captured.recurrence)
    assertEquals(true, reminderSlot.captured.isActive)
    assertEquals(null, reminderSlot.captured.groupId)
  }

  @Test
  fun `saveNote still activates a reminder without a group when there is no default reminder group`() {
    val viewModel = buildViewModel()
    viewModel.onReminderAttachedChanged(true)

    viewModel.saveNote()

    val reminderSlot = slot<ReminderV2>()
    coVerify(exactly = 1) { activateReminderUseCase(capture(reminderSlot), any()) }
    assertEquals(null, reminderSlot.captured.groupId)
    coVerify(exactly = 1) { saveNoteUseCase(any()) }
    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.MoveBack, event)
  }

  @Test
  fun `saveNote reuses the existing linked reminder identity when editing a note that already has one`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "42")
    val existingReminder = ReminderV2(
      uuId = "r1",
      noteId = "42",
      isActive = true,
      isRemoved = false,
      schedule = ReminderSchedule(
        startDateTime = LocalDateTime.of(2026, 7, 1, 9, 0),
        eventDateTime = LocalDateTime.of(2026, 7, 1, 9, 0),
      ),
    )
    coEvery { reminderV2Repository.getByNoteId("42") } returns listOf(existingReminder)
    coEvery { reminderV2Repository.getById("r1") } returns existingReminder
    val viewModel = buildViewModel(id = "42")

    viewModel.saveNote()

    val reminderSlot = slot<ReminderV2>()
    coVerify(exactly = 1) { activateReminderUseCase(capture(reminderSlot), any()) }
    assertEquals("r1", reminderSlot.captured.uuId)
  }

  @Test
  fun `saveNote deletes the previously linked reminder when the reminder switch is turned off`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "42")
    val existingReminder = ReminderV2(
      uuId = "r1",
      noteId = "42",
      isActive = true,
      isRemoved = false,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )
    coEvery { reminderV2Repository.getByNoteId("42") } returns listOf(existingReminder)
    coEvery { reminderV2Repository.getById("r1") } returns existingReminder
    val viewModel = buildViewModel(id = "42")
    // Loading the note attached the reminder; the user now turns the switch off before saving.
    assertEquals(true, viewModel.state.value.isReminderAttached)
    viewModel.onReminderAttachedChanged(false)

    viewModel.saveNote()

    coVerify(exactly = 1) { deleteReminderUseCase(existingReminder) }
    coVerify(exactly = 0) { activateReminderUseCase(any(), any()) }
  }

  @Test
  fun `saveNote shows an error and does not save when the reminder date and time are in the past`() {
    every { textProvider.getText(R.string.reminder_is_outdated) } returns "Outdated"
    every { dateTimeManager.isCurrent(any<LocalDateTime>()) } returns false
    val viewModel = buildViewModel()
    viewModel.onReminderAttachedChanged(true)

    viewModel.saveNote()

    coVerify(exactly = 0) { saveNoteUseCase(any()) }
    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Outdated"), event)
  }

  @Test
  fun `saveNote with newId assigns a fresh key and does not look up a reminder to delete`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "42")
    val viewModel = buildViewModel(id = "42")
    val saved = slot<NoteWithImages>()
    coEvery { saveNoteUseCase(capture(saved)) } returns Unit

    viewModel.saveNote(newId = true)

    assertNotEquals("42", saved.captured.note?.key)
    coVerify(exactly = 0) { reminderV2Repository.getById(any()) }
  }
}
