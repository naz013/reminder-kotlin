package com.github.naz013.appwidgets.singlenote

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.github.naz013.appwidgets.singlenote.adapter.RecyclableUiNoteWidgetAdapter
import com.github.naz013.appwidgets.singlenote.data.SearchableNotesData
import com.github.naz013.appwidgets.singlenote.data.UiNoteListSelectableAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteWidget
import com.github.naz013.appwidgets.singlenote.drawable.NoteDrawableParams
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.ui.common.adjustAlpha
import com.github.naz013.usecase.notes.GetAllNotesUseCase
import com.github.naz013.usecase.notes.GetNoteByIdUseCase
import com.github.naz013.usecase.notes.SearchNotesByTextUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class SingleNoteWidgetConfigViewModel(
  private val dispatcherProvider: DispatcherProvider,
  getAllNotesUseCase: GetAllNotesUseCase,
  searchNotesByTextUseCase: SearchNotesByTextUseCase,
  private val uiNoteListSelectableAdapter: UiNoteListSelectableAdapter,
  private val uiNoteWidgetAdapter: RecyclableUiNoteWidgetAdapter,
  private val getNoteByIdUseCase: GetNoteByIdUseCase
) : ViewModel(), DefaultLifecycleObserver {

  private val _previewBitmap = mutableLiveDataOf<UiNoteWidget>()
  val previewBitmap = _previewBitmap.toLiveData()

  private val notesData = SearchableNotesData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    getAllNotesUseCase = getAllNotesUseCase,
    searchNotesByTextUseCase = searchNotesByTextUseCase
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
      val noteWithImages = getNoteByIdUseCase(id) ?: return@launch
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
