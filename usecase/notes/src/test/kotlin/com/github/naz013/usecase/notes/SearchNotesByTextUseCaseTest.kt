package com.github.naz013.usecase.notes

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchNotesByTextUseCaseTest {

  @Test
  fun `invoke defaults to searching non-archived notes`() = runTest {
    val repository = mockk<NoteRepository>()
    val notes = listOf(NoteWithImages())
    coEvery { repository.searchByText(query = "milk", isArchived = false) } returns notes
    val useCase = SearchNotesByTextUseCase(repository)

    val result = useCase("milk")

    assertEquals(notes, result)
  }

  @Test
  fun `invoke forwards the archived flag when given`() = runTest {
    val repository = mockk<NoteRepository>()
    val notes = listOf(NoteWithImages())
    coEvery { repository.searchByText(query = "milk", isArchived = true) } returns notes
    val useCase = SearchNotesByTextUseCase(repository)

    val result = useCase("milk", isArchived = true)

    assertEquals(notes, result)
  }
}
