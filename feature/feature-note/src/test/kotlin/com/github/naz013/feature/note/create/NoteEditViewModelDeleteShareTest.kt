package com.github.naz013.feature.note.create

import androidx.compose.ui.text.input.TextFieldValue
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.share.NoteShareFormat
import com.github.naz013.feature.note.share.NoteShareText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

/**
 * Covers [NoteEditViewModel.onDeleteRequested] (delete) and [NoteEditViewModel.onShareClick]
 * (share). Both talk to their own mocked use case / repository and post a one-shot [event].
 */
internal class NoteEditViewModelDeleteShareTest : NoteEditViewModelTestSupport() {

  @Test
  fun `onDeleteRequested does nothing for a note that was never saved`() {
    val viewModel = buildViewModel(id = null)

    viewModel.onDeleteRequested()

    verify(exactly = 0) { pendingDeleteCoordinator.markPending(any(), any(), any(), any()) }
    assertNull(viewModel.event.value)
  }

  @Test
  fun `onDeleteRequested marks the note pending without a dialog or delete and posts Deleted`() {
    every { textProvider.getText(R.string.note_deleted) } returns "Note deleted"
    val viewModel = buildViewModel(id = "42")

    viewModel.onDeleteRequested()

    verify(exactly = 1) {
      pendingDeleteCoordinator.markPending(PendingEntity.NOTE, "42", "Note deleted", any())
    }
    coVerify(exactly = 0) { deleteNoteUseCase(any()) }
    assertNull(viewModel.state.value.activeDialog)
    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Deleted("42"), event)
  }

  @Test
  fun `committing the pending note delete deletes it and updates the widgets`() {
    val onCommit = slot<suspend () -> Unit>()
    every { pendingDeleteCoordinator.markPending(any(), any(), any(), capture(onCommit)) } just runs
    val viewModel = buildViewModel(id = "42")
    viewModel.onDeleteRequested()

    runBlocking { onCommit.captured.invoke() }

    coVerify(exactly = 1) { deleteNoteUseCase("42") }
    verify(exactly = 1) { appWidgetUpdater.updateNotesWidget() }
    verify(exactly = 1) { appWidgetUpdater.updateAllWidgets() }
  }

  @Test
  fun `onDeleteRequested does nothing when the note is already pending`() {
    val viewModel = buildViewModel(id = "42")
    pendingIds.value = setOf("42")

    viewModel.onDeleteRequested()

    verify(exactly = 0) { pendingDeleteCoordinator.markPending(any(), any(), any(), any()) }
    assertNull(viewModel.event.value)
  }

  @Test
  fun `onShareClick emits ShareNote with the trimmed text and the created file when it is readable`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("  Note body  "))
    val file = mockk<File>(relaxed = true)
    every { file.exists() } returns true
    every { file.canRead() } returns true
    coEvery { createSharedNoteFileUseCase(any()) } returns file

    viewModel.onShareFormatSelected(NoteShareFormat.ORIGINAL)

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.ShareNote(text = "Note body", file = file), event)
  }

  @Test
  fun `onShareClick shows an error when the file could not be created`() {
    every { textProvider.getText(R.string.error_sending) } returns "Cannot share"
    coEvery { createSharedNoteFileUseCase(any()) } returns null
    val viewModel = buildViewModel()

    viewModel.onShareFormatSelected(NoteShareFormat.ORIGINAL)

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
    viewModel.onShareFormatSelected(NoteShareFormat.ORIGINAL)

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
    viewModel.onShareFormatSelected(NoteShareFormat.ORIGINAL)

    val event = viewModel.event.value?.getContentIfNotHandled()
    assertEquals(NoteEditViewModel.ViewModelEvent.Error("Cannot share"), event)
  }

  @Test
  fun `onShareClick opens the share as sheet`() {
    val viewModel = buildViewModel()

    viewModel.onShareClick()

    assertEquals(NoteEditDialog.SHARE_AS, viewModel.state.value.activeDialog)
  }

  @Test
  fun `onShareFormatSelected closes the sheet and emits the markdown of the current text`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("Note body"))
    viewModel.onShareClick()

    viewModel.onShareFormatSelected(NoteShareFormat.MARKDOWN)

    assertNull(viewModel.state.value.activeDialog)
    val event = viewModel.event.value?.getContentIfNotHandled()
    val expected = NoteShareText(subject = "Note body", text = "Note body", mimeType = "text/plain")
    assertEquals(NoteEditViewModel.ViewModelEvent.ShareText(expected), event)
  }

  @Test
  fun `onShareFormatSelected html emits text html`() {
    val viewModel = buildViewModel()
    viewModel.onTextFieldValueChange(TextFieldValue("Note body"))

    viewModel.onShareFormatSelected(NoteShareFormat.HTML)

    val shared = (viewModel.event.value?.getContentIfNotHandled() as NoteEditViewModel.ViewModelEvent.ShareText).content
    assertEquals("text/html", shared.mimeType)
    assertEquals(true, shared.text.contains("<p>Note body</p>"))
  }
}
