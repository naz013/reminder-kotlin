package com.github.naz013.appwidgets.singlenote.data

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.SearchableLiveData
import com.github.naz013.usecase.notes.GetAllNotesUseCase
import com.github.naz013.usecase.notes.SearchNotesByTextUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

internal class SearchableNotesData(
  dispatcherProvider: DispatcherProvider,
  parentScope: CoroutineScope,
  private val getAllNotesUseCase: GetAllNotesUseCase,
  private val searchNotesByTextUseCase: SearchNotesByTextUseCase
) : SearchableLiveData<List<NoteWithImages>>(parentScope + dispatcherProvider.default()) {

  override suspend fun runQuery(query: String): List<NoteWithImages> {
    return if (query.isEmpty()) {
      getAllNotesUseCase(isArchived = false)
    } else {
      searchNotesByTextUseCase(query = query.lowercase(), isArchived = false)
    }
  }
}
