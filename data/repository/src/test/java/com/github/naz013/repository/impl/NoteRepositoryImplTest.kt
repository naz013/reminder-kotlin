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
  fun `delete removes the note and its version history in one transaction`() = runTest {
    repository.delete("n-1")

    verify(exactly = 1) { dao.deleteWithVersions("n-1") }
    verify(exactly = 0) { dao.delete(any()) }
  }

  @Test
  fun `deleteAllNotes removes every note and version in one transaction`() = runTest {
    repository.deleteAllNotes()

    verify(exactly = 1) { dao.deleteAllNotesWithVersions() }
    verify(exactly = 0) { dao.deleteAllNotes() }
  }
}
