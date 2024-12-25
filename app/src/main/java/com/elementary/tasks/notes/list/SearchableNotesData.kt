package com.elementary.tasks.notes.list

import com.github.naz013.feature.common.livedata.SearchableLiveData
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

class SearchableNotesData(
  dispatcherProvider: DispatcherProvider,
  parentScope: CoroutineScope,
  private val noteRepository: NoteRepository,
  private val isArchived: Boolean = false
) : SearchableLiveData<List<NoteWithImages>>(parentScope + dispatcherProvider.default()) {

  override suspend fun runQuery(query: String): List<NoteWithImages> {
    return if (query.isEmpty()) {
      noteRepository.getAll(isArchived = isArchived)
    } else {
      noteRepository.searchByText(query.lowercase(), isArchived = isArchived)
    }
  }
}
