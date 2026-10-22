package com.github.naz013.feature.note.compare

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.usecase.RestoreNoteVersionUseCase
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.NoteVersionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class CompareNoteVersionViewModel(
  private val noteKey: String,
  private val versionId: Long,
  private val dispatcherProvider: DispatcherProvider,
  private val noteRepository: NoteRepository,
  private val noteVersionRepository: NoteVersionRepository,
  private val restoreNoteVersionUseCase: RestoreNoteVersionUseCase,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(CompareNoteVersionState())
  val state: StateFlow<CompareNoteVersionState> = _state.asStateFlow()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      val version = noteVersionRepository.getById(versionId)?.takeIf { it.noteKey == noteKey }
      val note = noteRepository.getById(noteKey)?.note
      val newState = if (version == null || note == null) {
        CompareNoteVersionState(status = CompareStatus.Unavailable)
      } else {
        val diff = diffLines(historyText = version.content.text, currentText = note.content.text)
        CompareNoteVersionState(
          status = CompareStatus.Loaded,
          historyLabel = dateTimeManager.getFullDateTime(version.savedAt),
          historyLines = diff.history,
          currentLines = diff.current,
          isIdentical = diff.isIdentical,
        )
      }
      withContext(dispatcherProvider.main()) { _state.update { newState } }
    }
  }

  fun onRestoreClick() {
    if (_state.value.status != CompareStatus.Loaded) return
    viewModelScope.launch(dispatcherProvider.default()) {
      val restored = restoreNoteVersionUseCase(versionId)
      val message = textProvider.getText(
        if (restored) R.string.note_version_restored else R.string.note_version_restore_failed
      )
      withContext(dispatcherProvider.main()) {
        event.emit(if (restored) ViewModelEvent.Restored(message) else ViewModelEvent.Message(message))
      }
    }
  }

  sealed interface ViewModelEvent {
    data class Message(
      val message: String,
    ) : ViewModelEvent

    /** The version was restored: show [message] and return to the note preview. */
    data class Restored(
      val message: String,
    ) : ViewModelEvent
  }
}
