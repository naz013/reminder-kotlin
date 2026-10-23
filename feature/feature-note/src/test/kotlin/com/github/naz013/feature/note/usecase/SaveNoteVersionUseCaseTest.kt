package com.github.naz013.feature.note.usecase

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteVersion
import com.github.naz013.repository.NoteVersionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveNoteVersionUseCaseTest {
  private val repository = mockk<NoteVersionRepository>(relaxed = true)
  private var now = 1_000_000L
  private lateinit var useCase: SaveNoteVersionUseCase

  private val old = NoteDocument(text = "Old")
  private val new = NoteDocument(text = "New")

  @Before
  fun setUp() {
    coEvery { repository.getByNoteKey(any()) } returns emptyList()
    useCase = SaveNoteVersionUseCase(repository) { now }
  }

  private fun version(
    savedAt: Long,
    content: NoteDocument = NoteDocument(text = "Older"),
  ) = NoteVersion(id = 1, noteKey = "n", content = content, savedAt = savedAt)

  @Test
  fun `stores the previous content as a version when content changed`() = runTest {
    val saved = useCase("n", old, new)

    assertTrue(saved)
    coVerify(exactly = 1) { repository.addAndPrune(NoteVersion(noteKey = "n", content = old, savedAt = now), 20) }
  }

  @Test
  fun `does not store a version when content is unchanged`() = runTest {
    val saved = useCase("n", old, old.copy())

    assertFalse(saved)
    coVerify(exactly = 0) { repository.add(any()) }
  }

  @Test
  fun `does not store a version when the previous text is empty`() = runTest {
    val saved = useCase("n", NoteDocument(), new)

    assertFalse(saved)
    coVerify(exactly = 0) { repository.add(any()) }
  }

  @Test
  fun `skips the snapshot when the newest version is less than a minute old`() = runTest {
    coEvery { repository.getByNoteKey("n") } returns listOf(version(savedAt = now - 59_000L))

    val saved = useCase("n", old, new)

    assertFalse(saved)
    coVerify(exactly = 0) { repository.add(any()) }
  }

  @Test
  fun `stores the snapshot when the newest version is a minute old or more`() = runTest {
    coEvery { repository.getByNoteKey("n") } returns listOf(version(savedAt = now - 60_000L))

    assertTrue(useCase("n", old, new))
  }

  @Test
  fun `stores the snapshot despite a recent version when forced`() = runTest {
    coEvery { repository.getByNoteKey("n") } returns listOf(version(savedAt = now - 1_000L))

    assertTrue(useCase("n", old, new, force = true))
  }

  @Test
  fun `adds and prunes to twenty versions in one repository call`() = runTest {
    useCase("n", old, new)

    coVerify(exactly = 1) { repository.addAndPrune(any(), 20) }
    coVerify(exactly = 0) { repository.add(any()) }
    coVerify(exactly = 0) { repository.prune(any(), any()) }
  }

  @Test
  fun `skips the snapshot when the newest version has identical text and spans even when forced`() = runTest {
    coEvery { repository.getByNoteKey("n") } returns listOf(version(savedAt = now - 120_000L, content = old.copy()))

    val saved = useCase("n", old, new, force = true)

    assertFalse(saved)
    coVerify(exactly = 0) { repository.addAndPrune(any(), any()) }
  }

  @Test
  fun `concurrent saves of the same content create a single version`() = runTest {
    val stored = mutableListOf<NoteVersion>()
    coEvery { repository.getByNoteKey("n") } answers { stored.toList() }
    coEvery { repository.addAndPrune(any(), any()) } answers {
      stored.add(0, firstArg())
      1L
    }

    val results = (1..5).map { async { useCase("n", old, new, force = true) } }.awaitAll()

    assertEquals(1, results.count { it })
    assertEquals(1, stored.size)
  }
}
