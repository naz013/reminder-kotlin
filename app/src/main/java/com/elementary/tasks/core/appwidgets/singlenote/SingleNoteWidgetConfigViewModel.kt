package com.elementary.tasks.core.appwidgets.singlenote

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.note.UiNoteListSelectableAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.repository.NoteRepository
import com.elementary.tasks.core.data.ui.note.UiNoteWidget
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.views.drawable.NoteDrawableParams
import com.elementary.tasks.notes.list.SearchableNotesData
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SingleNoteWidgetConfigViewModel(
  dispatcherProvider: DispatcherProvider,
  private val notesDao: NotesDao,
  private val uiNoteListSelectableAdapter: UiNoteListSelectableAdapter,
  noteRepository: NoteRepository,
  private val uiNoteWidgetAdapter: RecyclableUiNoteWidgetAdapter
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

  private var previewJob: Job? = null

  override fun onCleared() {
    super.onCleared()
    uiNoteWidgetAdapter.clear()
  }

  fun createPreview(
    id: String,
    verticalAlignment: NoteDrawableParams.VerticalAlignment,
    horizontalAlignment: NoteDrawableParams.HorizontalAlignment,
    textSize: Float
  ) {
    previewJob?.cancel()
    previewJob = viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = notesDao.getById(id) ?: return@launch
      if (!isActive) return@launch
      val preview = uiNoteWidgetAdapter.convertDp(
        noteWithImages = noteWithImages,
        sizeDp = 156,
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        fontSize = textSize,
        marginDp = 8
      )
      if (!isActive) return@launch
      _previewBitmap.postValue(preview)
    }
  }

  fun onSearchUpdate(query: String) {
    notesData.onNewQuery(query)
  }
}
