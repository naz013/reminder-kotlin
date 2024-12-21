package com.elementary.tasks.core.appwidgets.singlenote

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.note.UiNoteListSelectableAdapter
import com.elementary.tasks.core.data.ui.note.UiNoteWidget
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.adjustAlpha
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.views.drawable.NoteDrawableParams
import com.elementary.tasks.notes.list.SearchableNotesData
import com.github.naz013.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SingleNoteWidgetConfigViewModel(
  dispatcherProvider: DispatcherProvider,
  private val noteRepository: NoteRepository,
  private val uiNoteListSelectableAdapter: UiNoteListSelectableAdapter,
  private val uiNoteWidgetAdapter: RecyclableUiNoteWidgetAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val _previewBitmap = mutableLiveDataOf<UiNoteWidget>()
  val previewBitmap = _previewBitmap.toLiveData()

  private val notesData = SearchableNotesData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
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
    textSize: Float,
    textColor: Int,
    textColorOpacity: Float,
    overlayColor: Int,
    overlayOpacity: Float
  ) {
    previewJob?.cancel()
    previewJob = viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id) ?: return@launch
      if (!isActive) return@launch
      val preview = uiNoteWidgetAdapter.convertDp(
        noteWithImages = noteWithImages,
        sizeDp = 156,
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        fontSize = textSize,
        marginDp = 8,
        overlayColor = overlayColor.adjustAlpha(overlayOpacity.toInt()),
        textColor = textColor.adjustAlpha(textColorOpacity.toInt())
      )
      if (!isActive) return@launch
      _previewBitmap.postValue(preview)
    }
  }

  fun onSearchUpdate(query: String) {
    notesData.onNewQuery(query)
  }
}
