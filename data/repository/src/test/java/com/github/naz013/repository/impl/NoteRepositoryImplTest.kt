package com.github.naz013.repository.impl

import com.github.naz013.repository.dao.NotesDao
import com.github.naz013.repository.observer.TableChangeNotifier
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NoteRepositoryImplTest {
  private val dao = mockk<NotesDao>(relaxed = true)
  private val tableChangeNotifier = mockk<TableChangeNotifier>(relaxed = true)
  private lateinit var repository: NoteRepositoryImpl

  @Before
  fun setUp() {
    repository = NoteRepositoryImpl(dao, tableChangeNotifier)
  }

  @Test
  fun `delete removes the note's version history too`() = runTest {
    repository.delete("n-1")

    verify { dao.delete("n-1") }
    verify { dao.deleteVersionsForNote("n-1") }
  }

  @Test
  fun `deleteAllNotes removes every version too`() = runTest {
    repository.deleteAllNotes()

    verify { dao.deleteAllNotes() }
    verify { dao.deleteAllVersions() }
  }
}
