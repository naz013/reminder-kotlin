package com.github.naz013.feature.note.usecase

import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TogglePinnedNoteUseCaseTest {
  private lateinit var noteRepository: NoteRepository
  private lateinit var scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase

  private lateinit var useCase: TogglePinnedNoteUseCase

  @Before
  fun setUp() {
    noteRepository = mockk(relaxed = true)
    scheduleBackgroundWorkUseCase = mockk(relaxed = true)
    useCase = TogglePinnedNoteUseCase(noteRepository, scheduleBackgroundWorkUseCase)
  }

  private fun note(id: String, isPinned: Boolean) =
    Note(key = id, summary = "Summary", isPinned = isPinned, version = 1L, syncState = SyncState.Synced)

  @Test
  fun `pins a note that was not pinned`() = runTest {
    coEvery { noteRepository.getById("id-1") } returns NoteWithImages(note = note("id-1", isPinned = false))

    useCase("id-1")

    coVerify(exactly = 1) {
      noteRepository.save(match<Note> { it.key == "id-1" && it.isPinned && it.version == 2L })
    }
    coVerify(exactly = 1) { noteRepository.updateSyncState("id-1", SyncState.WaitingForUpload) }
  }

  @Test
  fun `unpins a note that was pinned`() = runTest {
    coEvery { noteRepository.getById("id-2") } returns NoteWithImages(note = note("id-2", isPinned = true))

    useCase("id-2")

    coVerify(exactly = 1) { noteRepository.save(match<Note> { it.key == "id-2" && !it.isPinned }) }
  }

  @Test
  fun `does nothing when note does not exist`() = runTest {
    coEvery { noteRepository.getById("missing") } returns null

    useCase("missing")

    coVerify(exactly = 0) { noteRepository.save(any<Note>()) }
    coVerify(exactly = 0) { noteRepository.updateSyncState(any(), any()) }
  }
}
