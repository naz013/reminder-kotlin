package com.elementary.tasks.notes.create

import androidx.compose.ui.text.input.TextFieldValue
import com.elementary.tasks.R
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

/**
 * Covers [NoteEditViewModel.onDeleteConfirmed] (delete) and [NoteEditViewModel.onShareClick]
 * (share). Both talk to their own mocked use case / repository and post a one-shot [event].
 */
class NoteEditViewModelDeleteShareTest : NoteEditViewModelTestSupport() {

  @Test
  fun `onDeleteConfirmed does nothing for a note that was never saved`() {
    val viewModel = buildViewModel(id = null)

    viewModel.onDeleteConfirmed()

    coVerify(exactly = 0) { deleteNoteUseCase(any()) }
    assertNull(viewModel.event.value)
  }

  @Test
  fun `onDeleteConfirmed shows an error when the note no longer exists in the repository`() {
    every { textProvider.getText(R.string.default_error_msg) } returns "Not found"
    coEvery { noteRepository.getById("42") } returns null
    val viewModel = buildViewModel(id = "42")

    viewModel.onDeleteConfirmed()

    coVerify(exactly = 0) { deleteNoteUseCase(any()) }
    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Not found"), event)
  }

  @Test
  fun `onDeleteConfirmed deletes the note, updates the widgets and posts MoveBack`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "42")
    val viewModel = buildViewModel(id = "42")

    viewModel.onDeleteConfirmed()

    coVerify(exactly = 1) { deleteNoteUseCase("42") }
    verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    verify(exactly = 1) { appWidgetUpdater.updateAllWidgets() }
    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.MoveBack, event)
  }

  @Test
  fun `onDeleteConfirmed clears the active dialog before deleting`() {
    val noteWithImages = NoteWithImages(note = Note(key = "42", syncState = SyncState.Synced))
    coEvery { noteRepository.getById("42") } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "42")
    val viewModel = buildViewModel(id = "42")
    viewModel.onDeleteRequested()

    viewModel.onDeleteConfirmed()

    assertNull(viewModel.state.value.activeDialog)
  }

  @Test
  fun `onShareClick emits ShareNote with the trimmed text and the created file when it is readable`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("  Note body  "))
    val file = mockk<File>(relaxed = true)
    every { file.exists() } returns true
    every { file.canRead() } returns true
    coEvery { createSharedNoteFileUseCase(any()) } returns file

    viewModel.onShareClick()

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.ShareNote(text = "Note body", file = file), event)
  }

  @Test
  fun `onShareClick shows an error when the file could not be created`() {
    every { textProvider.getText(R.string.error_sending) } returns "Cannot share"
    coEvery { createSharedNoteFileUseCase(any()) } returns null
    val viewModel = buildViewModel()

    viewModel.onShareClick()

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Cannot share"), event)
  }

  @Test
  fun `onShareClick shows an error when the created file exists but cannot be read`() {
    every { textProvider.getText(R.string.error_sending) } returns "Cannot share"
    val file = mockk<File>(relaxed = true)
    every { file.exists() } returns true
    every { file.canRead() } returns false
    coEvery { createSharedNoteFileUseCase(any()) } returns file

    val viewModel = buildViewModel()
    viewModel.onShareClick()

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Cannot share"), event)
  }

  @Test
  fun `onShareClick shows an error when the created file does not exist`() {
    every { textProvider.getText(R.string.error_sending) } returns "Cannot share"
    val file = mockk<File>(relaxed = true)
    every { file.exists() } returns false
    coEvery { createSharedNoteFileUseCase(any()) } returns file

    val viewModel = buildViewModel()
    viewModel.onShareClick()

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Cannot share"), event)
  }
}
