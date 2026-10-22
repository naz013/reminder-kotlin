package com.github.naz013.repository.impl

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteVersion
import com.github.naz013.repository.dao.NoteVersionDao
import com.github.naz013.repository.entity.NoteVersionEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NoteVersionRepositoryImplTest {
  private val dao = mockk<NoteVersionDao>(relaxed = true)
  private lateinit var repository: NoteVersionRepositoryImpl

  @Before
  fun setUp() {
    repository = NoteVersionRepositoryImpl(dao)
  }

  @Test
  fun `add maps the content to entity columns and returns the generated id`() = runTest {
    val entity = slot<NoteVersionEntity>()
    every { dao.insert(capture(entity)) } returns 7L

    val id = repository.add(NoteVersion(noteKey = "n-1", content = NoteDocument(text = "old"), savedAt = 100L))

    assertEquals(7L, id)
    assertEquals("n-1", entity.captured.noteKey)
    assertEquals("old", entity.captured.text)
    assertEquals(100L, entity.captured.savedAt)
  }

  @Test
  fun `getByNoteKey maps entities back to domain keeping the DAO order`() = runTest {
    every { dao.getByNoteKey("n-1") } returns listOf(
      NoteVersionEntity(id = 2, noteKey = "n-1", text = "b", spans = emptyList(), savedAt = 200L),
      NoteVersionEntity(id = 1, noteKey = "n-1", text = "a", spans = emptyList(), savedAt = 100L),
    )

    val result = repository.getByNoteKey("n-1")

    assertEquals(listOf("b", "a"), result.map { it.content.text })
    assertEquals(listOf(2L, 1L), result.map { it.id })
  }

  @Test
  fun `getById returns null when no row exists`() = runTest {
    every { dao.getById(5L) } returns null

    assertNull(repository.getById(5L))
  }

  @Test
  fun `prune and delete calls are forwarded to the DAO`() = runTest {
    repository.prune("n-1", 20)
    repository.deleteByNoteKey("n-1")
    repository.deleteAll()

    verify { dao.prune("n-1", 20) }
    verify { dao.deleteByNoteKey("n-1") }
    verify { dao.deleteAll() }
  }
}
