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

class UpdateNoteUseCaseTest {

  private val noteRepository = mockk<NoteRepository>(relaxUnitFun = true)
  private val useCase = UpdateNoteUseCase(noteRepository)

  @Test
  fun `invoke updates content, bumps version and marks for upload when the note exists`() = runTest {
    val existing = Note(key = "note-1", syncState = SyncState.Synced, version = 4L)
    coEvery { noteRepository.getById("note-1") } returns NoteWithImages(note = existing)

    val result = useCase(id = "note-1", title = "Wi-Fi password", content = "hunter2")

    assertEquals("Wi-Fi password\nhunter2", result?.content?.text)
    assertEquals(5L, result?.version)
    coVerify { noteRepository.save(result!!) }
    coVerify { noteRepository.updateSyncState("note-1", SyncState.WaitingForUpload) }
  }

  @Test
  fun `invoke returns null and does not save when no note exists`() = runTest {
    coEvery { noteRepository.getById("missing") } returns null

    val result = useCase(id = "missing", title = "Title", content = "Content")

    assertNull(result)
    coVerify(exactly = 0) { noteRepository.save(any<Note>()) }
    coVerify(exactly = 0) { noteRepository.updateSyncState(any(), any()) }
  }
}
