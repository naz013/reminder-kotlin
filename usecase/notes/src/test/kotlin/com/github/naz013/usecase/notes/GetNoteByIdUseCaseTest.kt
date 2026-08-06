package com.github.naz013.usecase.notes

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetNoteByIdUseCaseTest {

  @Test
  fun `invoke returns the note for the given id`() = runTest {
    val repository = mockk<NoteRepository>()
    val note = NoteWithImages()
    coEvery { repository.getById("1") } returns note
    val useCase = GetNoteByIdUseCase(repository)

    val result = useCase("1")

    assertEquals(note, result)
  }

  @Test
  fun `invoke returns null when the repository has no match`() = runTest {
    val repository = mockk<NoteRepository>()
    coEvery { repository.getById("missing") } returns null
    val useCase = GetNoteByIdUseCase(repository)

    val result = useCase("missing")

    assertNull(result)
  }
}
