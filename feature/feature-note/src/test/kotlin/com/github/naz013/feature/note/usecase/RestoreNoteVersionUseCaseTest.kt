package com.github.naz013.feature.note.usecase

import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteVersion
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.NoteVersionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RestoreNoteVersionUseCaseTest {
  private val noteRepository = mockk<NoteRepository>(relaxed = true)
  private val versionRepository = mockk<NoteVersionRepository>(relaxed = true)
  private val saveNoteVersionUseCase = mockk<SaveNoteVersionUseCase>(relaxed = true)
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)
  private lateinit var useCase: RestoreNoteVersionUseCase

  private val current = NoteDocument(text = "Current")
  private val older = NoteDocument(text = "Older")

  @Before
  fun setUp() {
    useCase = RestoreNoteVersionUseCase(
      noteRepository,
      versionRepository,
      saveNoteVersionUseCase,
      scheduleBackgroundWorkUseCase,
    )
    coEvery { versionRepository.getById(5L) } returns NoteVersion(id = 5, noteKey = "n", content = older, savedAt = 1L)
    coEvery { noteRepository.getById("n") } returns NoteWithImages(
      note = Note(key = "n", content = current, version = 3L, syncState = SyncState.Synced),
    )
  }

  @Test
  fun `snapshots the current content before replacing it with the version`() = runTest {
    val restored = useCase(5L)

    assertTrue(restored)
    coVerifyOrder {
      saveNoteVersionUseCase("n", current, older, true)
      noteRepository.save(match<Note> { it.content == older })
    }
  }

  @Test
  fun `marks the note for upload and bumps its version`() = runTest {
    useCase(5L)

    coVerify(exactly = 1) {
      noteRepository.save(match<Note> { it.version == 4L && it.syncState == SyncState.WaitingForUpload })
    }
    coVerify(exactly = 1) { noteRepository.updateSyncState("n", SyncState.WaitingForUpload) }
    coVerify(exactly = 1) { scheduleBackgroundWorkUseCase(any(), any(), "n") }
  }

  @Test
  fun `returns false and changes nothing when the version does not exist`() = runTest {
    coEvery { versionRepository.getById(9L) } returns null

    assertFalse(useCase(9L))
    coVerify(exactly = 0) { noteRepository.save(any<Note>()) }
  }

  @Test
  fun `returns false when the note no longer exists`() = runTest {
    coEvery { noteRepository.getById("n") } returns null

    assertFalse(useCase(5L))
    coVerify(exactly = 0) { noteRepository.save(any<Note>()) }
  }
}
