package com.github.naz013.appfunctions.note

import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeleteNoteUseCaseTest {

  private val noteRepository = mockk<NoteRepository>(relaxUnitFun = true)
  private val useCase = DeleteNoteUseCase(noteRepository)

  @Test
  fun `invoke deletes and returns the note when it exists`() = runTest {
    val note = Note(key = "note-1", syncState = SyncState.Synced)
    coEvery { noteRepository.getById("note-1") } returns NoteWithImages(note = note)

    val result = useCase("note-1")

    assertEquals(note, result)
    coVerify { noteRepository.delete("note-1") }
  }

  @Test
  fun `invoke returns null and does not delete when no note exists`() = runTest {
    coEvery { noteRepository.getById("missing") } returns null

    val result = useCase("missing")

    assertNull(result)
    coVerify(exactly = 0) { noteRepository.delete(any()) }
  }
}
