package com.github.naz013.appwidgets.singlenote

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.singlenote.adapter.RecyclableUiNoteWidgetAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteWidgetAdapter
import com.github.naz013.appwidgets.singlenote.drawable.NoteDrawableParams
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.ui.note.UiNoteListItemAdapter
import com.github.naz013.ui.common.adjustAlpha
import com.github.naz013.usecase.notes.GetAllNotesUseCase
import com.github.naz013.usecase.notes.GetNoteByIdUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class SingleNoteWidgetConfigViewModel(
  private val appWidgetUpdater: AppWidgetUpdater,
  private val dispatcherProvider: DispatcherProvider,
  private val prefsProvider: SingleNoteWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val getAllNotesUseCase: GetAllNotesUseCase,
  private val getNoteByIdUseCase: GetNoteByIdUseCase,
  private val uiNoteListItemAdapter: UiNoteListItemAdapter,
  private val previewAdapter: RecyclableUiNoteWidgetAdapter,
  private val uiNoteWidgetAdapter: UiNoteWidgetAdapter,
  appWidgetPreferences: AppWidgetPreferences,
  private val noteWidgetPreferences: NoteWidgetPreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(
    SingleNoteWidgetConfigState(
      textSize = prefsProvider.getTextSize().takeIf { it in 1f..250f } ?: 16f,
      horizontalAlignment = prefsProvider.getHorizontalAlignment(),
      verticalAlignment = prefsProvider.getVerticalAlignment(),
      textColorIndex = prefsProvider.getTextColorPosition(),
      textColorOpacity = prefsProvider.getTextColorOpacity(),
      overlayColorIndex = prefsProvider.getOverlayColorPosition(),
      overlayColorOpacity = prefsProvider.getOverlayColorOpacity(),
      hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled,
      palette = noteWidgetPreferences.getNoteColors(),
    )
  )
  val state = _state.asStateFlow()

  private val _events = Channel<SingleNoteWidgetConfigEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  private var previewJob: Job? = null

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      val notes = getAllNotesUseCase(isArchived = false).map { uiNoteListItemAdapter.convert(it) }
      _state.update { it.copy(notes = notes) }

      val storedId = prefsProvider.getNoteId()
      if (storedId != null && notes.any { it.id == storedId }) {
        selectNote(storedId)
      }
    }
  }

  override fun onCleared() {
    previewAdapter.clear()
  }

  fun onNoteSelected(id: String) {
    selectNote(id)
  }

  private fun selectNote(id: String) {
    _state.update { it.copy(selectedNoteId = id) }
    updatePreview()
  }

  fun onTextSizeChanged(size: Float) {
    _state.update { it.copy(textSize = size) }
    updatePreview()
  }

  fun onHorizontalAlignmentChanged(alignment: NoteDrawableParams.HorizontalAlignment) {
    _state.update { it.copy(horizontalAlignment = alignment) }
    updatePreview()
  }

  fun onVerticalAlignmentChanged(alignment: NoteDrawableParams.VerticalAlignment) {
    _state.update { it.copy(verticalAlignment = alignment) }
    updatePreview()
  }

  fun onTextColorSelected(index: Int) {
    _state.update { it.copy(textColorIndex = index) }
    updatePreview()
  }

  fun onTextColorOpacityChanged(opacity: Float) {
    _state.update { it.copy(textColorOpacity = opacity) }
    updatePreview()
  }

  fun onOverlayColorSelected(index: Int) {
    _state.update { it.copy(overlayColorIndex = index) }
    updatePreview()
  }

  fun onOverlayColorOpacityChanged(opacity: Float) {
    _state.update { it.copy(overlayColorOpacity = opacity) }
    updatePreview()
  }

  fun onSaveClick() {
    val noteId = state.value.selectedNoteId
    if (noteId == null) {
      _events.trySend(SingleNoteWidgetConfigEvent.NoteNotSelected)
      return
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      val s = state.value
      prefsProvider.setNoteId(noteId)
      prefsProvider.setHorizontalAlignment(s.horizontalAlignment)
      prefsProvider.setVerticalAlignment(s.verticalAlignment)
      prefsProvider.setTextSize(s.textSize)
      prefsProvider.setTextColorPosition(s.textColorIndex)
      prefsProvider.setTextColorOpacity(s.textColorOpacity)
      prefsProvider.setOverlayColorPosition(s.overlayColorIndex)
      prefsProvider.setOverlayColorOpacity(s.overlayColorOpacity)

      analyticsEventSender.send(WidgetUsedEvent(Widget.SINGLE_NOTE))

      appWidgetUpdater.updateSingleNoteWidget(prefsProvider.widgetId)

      _events.trySend(SingleNoteWidgetConfigEvent.Saved)
    }
  }

  private fun colorForIndex(index: Int): Int {
    val palette = _state.value.palette
    if (palette.isEmpty()) return android.graphics.Color.BLACK
    // A previously-persisted index can fall outside the current palette if the palette's size
    // ever changes between saves (e.g. a widget saved under an older palette) - coerce rather
    // than crash on stale prefs.
    return palette[index.coerceIn(palette.indices)].toArgb()
  }

  private fun updatePreview() {
    val s = state.value
    val noteId = s.selectedNoteId ?: return
    previewJob?.cancel()
    previewJob = viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = getNoteByIdUseCase(noteId) ?: return@launch
      if (!isActive) return@launch
      val preview = previewAdapter.convertDp(
        noteWithImages = noteWithImages,
        sizeDp = 156,
        verticalAlignment = s.verticalAlignment,
        horizontalAlignment = s.horizontalAlignment,
        fontSize = s.textSize,
        marginDp = 8,
        overlayColor = colorForIndex(s.overlayColorIndex).adjustAlpha(s.overlayColorOpacity.toInt()),
        textColor = colorForIndex(s.textColorIndex).adjustAlpha(s.textColorOpacity.toInt()),
      )
      if (!isActive) return@launch
      _state.update { it.copy(previewBitmap = preview.bitmap) }
    }
  }
}
