package com.elementary.tasks.core.app_widgets.singlenote

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.note.UiNoteListSelectableAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteWidgetAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.repository.NoteRepository
import com.elementary.tasks.core.data.ui.note.UiNoteWidget
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.notes.list.SearchableNotesData
import kotlinx.coroutines.launch

class SingleNoteWidgetConfigViewModel(
  dispatcherProvider: DispatcherProvider,
  private val notesDao: NotesDao,
  private val uiNoteListSelectableAdapter: UiNoteListSelectableAdapter,
  noteRepository: NoteRepository,
  private val uiNoteWidgetAdapter: UiNoteWidgetAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val _previewBitmap = mutableLiveDataOf<UiNoteWidget>()
  val previewBitmap = _previewBitmap.toLiveData()

  private val notesData = SearchableNotesData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    notesDao = notesDao,
    noteRepository = noteRepository,
    isArchived = false
  )
  val notes = notesData.map { list ->
    list.map { uiNoteListSelectableAdapter.convert(it) }
  }

  fun createPreview(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = notesDao.getById(id) ?: return@launch
      _previewBitmap.postValue(uiNoteWidgetAdapter.convertDp(noteWithImages, 156, 156))
    }
  }

  fun onSearchUpdate(query: String) {
    notesData.onNewQuery(query)
  }
}
