package com.github.naz013.usecase.notes

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllNotesUseCaseTest {

  @Test
  fun `invoke defaults to non-archived notes`() = runTest {
    val repository = mockk<NoteRepository>()
    val notes = listOf(NoteWithImages())
    coEvery { repository.getAll(isArchived = false) } returns notes
    val useCase = GetAllNotesUseCase(repository)

    val result = useCase()

    assertEquals(notes, result)
  }

  @Test
  fun `invoke forwards the archived flag when given`() = runTest {
    val repository = mockk<NoteRepository>()
    val notes = listOf(NoteWithImages())
    coEvery { repository.getAll(isArchived = true) } returns notes
    val useCase = GetAllNotesUseCase(repository)

    val result = useCase(isArchived = true)

    assertEquals(notes, result)
  }
}
