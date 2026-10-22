package com.github.naz013.feature.note.compare

import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteVersion
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.usecase.RestoreNoteVersionUseCase
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.NoteVersionRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompareNoteVersionViewModelTest : BaseTest() {
  private val noteKey = "n1"
  private val versionId = 7L

  private val noteRepository = mockk<NoteRepository>()
  private val noteVersionRepository = mockk<NoteVersionRepository>()
  private val restoreNoteVersionUseCase = mockk<RestoreNoteVersionUseCase>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val textProvider = mockk<TextProvider>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getFullDateTime(any<Long>()) } returns "when"
  }

  private fun stubNote(text: String?) {
    coEvery { noteRepository.getById(noteKey) } returns text?.let {
      NoteWithImages(note = Note(key = noteKey, content = NoteDocument(text = it), syncState = SyncState.Synced))
    }
  }

  private fun stubVersion(text: String?, forKey: String = noteKey) {
    coEvery { noteVersionRepository.getById(versionId) } returns text?.let {
      NoteVersion(id = versionId, noteKey = forKey, content = NoteDocument(text = it), savedAt = 5L)
    }
  }

  private fun createViewModel() = CompareNoteVersionViewModel(
    noteKey = noteKey,
    versionId = versionId,
    dispatcherProvider = mockDispatcherProvider(),
    noteRepository = noteRepository,
    noteVersionRepository = noteVersionRepository,
    restoreNoteVersionUseCase = restoreNoteVersionUseCase,
    dateTimeManager = dateTimeManager,
    textProvider = textProvider,
  )

  @Test
  fun `state is loading until the repositories answer`() {
    coEvery { noteRepository.getById(noteKey) } coAnswers { awaitCancellation() }
    stubVersion("old")

    assertEquals(CompareStatus.Loading, createViewModel().state.value.status)
  }

  @Test
  fun `loads version and current note into a diff`() {
    stubVersion("a\nold")
    stubNote("a\nnew")

    val state = createViewModel().state.value

    assertEquals(CompareStatus.Loaded, state.status)
    assertEquals("when", state.historyLabel)
    assertEquals(listOf(LineChange.Same, LineChange.Removed), state.historyLines.map { it.change })
    assertEquals(listOf(LineChange.Same, LineChange.Added), state.currentLines.map { it.change })
    assertFalse(state.isIdentical)
  }

  @Test
  fun `reports identical when texts match`() {
    stubVersion("same")
    stubNote("same")

    assertTrue(createViewModel().state.value.isIdentical)
  }

  @Test
  fun `is unavailable when the version is missing`() {
    stubVersion(null)
    stubNote("text")

    assertEquals(CompareStatus.Unavailable, createViewModel().state.value.status)
  }

  @Test
  fun `is unavailable when the note is missing`() {
    stubVersion("old")
    stubNote(null)

    assertEquals(CompareStatus.Unavailable, createViewModel().state.value.status)
  }

  @Test
  fun `is unavailable when the version belongs to another note`() {
    stubVersion("old", forKey = "other")
    stubNote("text")

    assertEquals(CompareStatus.Unavailable, createViewModel().state.value.status)
  }

  @Test
  fun `restore emits Restored so the screen navigates back`() {
    stubVersion("old")
    stubNote("new")
    every { textProvider.getText(R.string.note_version_restored) } returns "Restored"
    coEvery { restoreNoteVersionUseCase(versionId) } returns true
    val viewModel = createViewModel()

    viewModel.onRestoreClick()

    coVerify(exactly = 1) { restoreNoteVersionUseCase(versionId) }
    assertEquals(
      CompareNoteVersionViewModel.ViewModelEvent.Restored("Restored"),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `failed restore reports a message and stays on the screen`() {
    stubVersion("old")
    stubNote("new")
    every { textProvider.getText(R.string.note_version_restore_failed) } returns "Failed"
    coEvery { restoreNoteVersionUseCase(versionId) } returns false
    val viewModel = createViewModel()

    viewModel.onRestoreClick()

    assertEquals(
      CompareNoteVersionViewModel.ViewModelEvent.Message("Failed"),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `restore does nothing when the data is unavailable`() {
    stubVersion(null)
    stubNote("text")
    val viewModel = createViewModel()

    viewModel.onRestoreClick()

    coVerify(exactly = 0) { restoreNoteVersionUseCase(any()) }
    assertNull(viewModel.event.value)
  }

  private suspend fun awaitCancellation(): Nothing = kotlinx.coroutines.awaitCancellation()
}
