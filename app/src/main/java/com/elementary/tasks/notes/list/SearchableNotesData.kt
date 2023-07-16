package com.elementary.tasks.notes.list

import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.livedata.SearchableLiveData
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.repository.NoteRepository
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

class SearchableNotesData(
  dispatcherProvider: DispatcherProvider,
  parentScope: CoroutineScope,
  private val notesDao: NotesDao,
  private val noteRepository: NoteRepository,
  private val isArchived: Boolean = false
) : SearchableLiveData<List<NoteWithImages>>(parentScope + dispatcherProvider.default()) {

  override fun runQuery(query: String): List<NoteWithImages> {
    return if (query.isEmpty()) {
      noteRepository.getAll(isArchived = isArchived)
    } else {
      notesDao.searchByText(query.lowercase(), isArchived = isArchived)
    }
  }
}
