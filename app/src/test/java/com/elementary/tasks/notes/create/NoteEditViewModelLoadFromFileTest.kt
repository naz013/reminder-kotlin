package com.elementary.tasks.notes.create

import android.content.ContentResolver
import android.net.Uri
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.notes.SharedNote
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Covers [NoteEditViewModel.loadFromFile], the path used when the note editor is opened to import
 * a previously-exported `.etnote` file. `MemoryUtil.readFromUri` is a companion-object ("static")
 * function called directly from the ViewModel, so it needs `mockkObject(MemoryUtil.Companion)`
 * rather than a constructor-injected mock (confirmed working empirically before writing these
 * tests; MemoryUtil itself is a plain utility class, not one of NoteEditViewModel's dependencies).
 */
class NoteEditViewModelLoadFromFileTest : NoteEditViewModelTestSupport() {

  @Before
  fun mockMemoryUtil() {
    mockkObject(MemoryUtil.Companion)
  }

  @After
  fun unmockMemoryUtil() {
    unmockkObject(MemoryUtil.Companion)
  }

  private fun contentUri(uriScheme: String): Uri {
    val uri = mockk<Uri>()
    every { uri.scheme } returns uriScheme
    return uri
  }

  @Test
  fun `does nothing when the uri scheme is content`() {
    val viewModel = buildViewModel()
    val uri = contentUri(ContentResolver.SCHEME_CONTENT)

    viewModel.loadFromFile(uri)

    verify(exactly = 0) { MemoryUtil.readFromUri(any(), any(), any()) }
    assertEquals(false, viewModel.state.value.isFromFile)
  }

  @Test
  fun `imports a valid shared note file and marks it as loaded from file`() {
    val viewModel = buildViewModel()
    val uri = contentUri("file")
    val sharedNote = SharedNote(id = "imported-1", text = "Imported body", title = "Imported title")
    every { MemoryUtil.readFromUri(fakeContext, uri, SharedNote.FILE_EXTENSION) } returns sharedNote
    val noteWithImages = NoteWithImages(note = Note(key = "imported-1", summary = "Imported body", syncState = SyncState.Synced))
    every { noteToOldNoteConverter.toNote(sharedNote) } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "imported-1", text = "Imported body")
    coEvery { noteRepository.getById("imported-1") } returns null

    viewModel.loadFromFile(uri)

    val state = viewModel.state.value
    assertEquals(true, state.isFromFile)
    assertEquals(false, state.hasSameInDb)
    assertEquals("Imported body", state.textFieldValue.text)
  }

  @Test
  fun `marks hasSameInDb true when the imported note already exists locally`() {
    val viewModel = buildViewModel()
    val uri = contentUri("file")
    val sharedNote = SharedNote(id = "dup")
    every { MemoryUtil.readFromUri(fakeContext, uri, SharedNote.FILE_EXTENSION) } returns sharedNote
    val noteWithImages = NoteWithImages(note = Note(key = "dup", syncState = SyncState.Synced))
    every { noteToOldNoteConverter.toNote(sharedNote) } returns noteWithImages
    every { uiNoteEditAdapter.convert(noteWithImages) } returns uiNoteEdit(id = "dup")
    coEvery { noteRepository.getById("dup") } returns noteWithImages

    viewModel.loadFromFile(uri)

    assertEquals(true, viewModel.state.value.hasSameInDb)
  }

  @Test
  fun `does nothing when the uri cannot be read into a shared note`() {
    val viewModel = buildViewModel()
    val uri = contentUri("file")
    every { MemoryUtil.readFromUri(fakeContext, uri, SharedNote.FILE_EXTENSION) } returns null

    viewModel.loadFromFile(uri)

    assertEquals(false, viewModel.state.value.isFromFile)
  }

  @Test
  fun `does nothing when the shared note fails to convert to a note`() {
    val viewModel = buildViewModel()
    val uri = contentUri("file")
    val sharedNote = SharedNote(id = "")
    every { MemoryUtil.readFromUri(fakeContext, uri, SharedNote.FILE_EXTENSION) } returns sharedNote
    every { noteToOldNoteConverter.toNote(sharedNote) } returns null

    viewModel.loadFromFile(uri)

    assertEquals(false, viewModel.state.value.isFromFile)
  }
}
