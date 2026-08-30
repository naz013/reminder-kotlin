package com.github.naz013.appwidgets.notes

import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.NoteRepository
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.note.NotePreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NotesAppWidgetViewModelTest {

  private val prefsProvider = mockk<NotesWidgetPrefsProvider>(relaxed = true)
  private val noteRepository = mockk<NoteRepository>()
  private val themeProvider = mockk<ThemeProvider>(relaxed = true)
  private val notePreferences = mockk<NotePreferences>()

  private fun note(key: String) = NoteWithImages(
    note = Note(key = key, syncState = SyncState.Synced),
  )

  private fun createViewModel() = NotesAppWidgetViewModel(
    prefsProvider = prefsProvider,
    noteRepository = noteRepository,
    themeProvider = themeProvider,
    notePreferences = notePreferences,
  )

  @Test
  fun `getState loads notes sorted by the user's current note order`() =
    runTest {
      every { notePreferences.noteOrder } returns "text_za"
      coEvery {
        noteRepository.getNotes(isArchived = false, query = "", sortOrder = "text_za")
      } returns listOf(note("1"), note("2"))
      val viewModel = createViewModel()

      val state = viewModel.getState()

      coVerify(exactly = 1) {
        noteRepository.getNotes(isArchived = false, query = "", sortOrder = "text_za")
      }
      assertEquals(listOf("1", "2"), state.items.map { it.uuId })
    }
}
