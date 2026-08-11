package com.github.naz013.feature.note.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.UiNoteNotificationAdapter
import com.github.naz013.ui.note.NoteNotifier
import com.github.naz013.ui.note.NotePreferences
import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.note.UiNoteListItemAdapter
import com.github.naz013.feature.note.preview.ImagesSingleton
import com.github.naz013.feature.note.usecase.ChangeNoteArchiveStateUseCase
import com.github.naz013.feature.note.usecase.CreateSharedNoteFileUseCase
import com.github.naz013.feature.note.usecase.DeleteNoteUseCase
import com.github.naz013.feature.note.usecase.SaveNoteUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NotesViewModel(
  private val isArchived: Boolean = false,
  private val dispatcherProvider: DispatcherProvider,
  private val textProvider: TextProvider,
  private val uiNoteListItemAdapter: UiNoteListItemAdapter,
  private val notePreferences: NotePreferences,
  private val noteRepository: NoteRepository,
  private val uiNoteNotificationAdapter: UiNoteNotificationAdapter,
  private val noteNotifier: NoteNotifier,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val deleteNoteUseCase: DeleteNoteUseCase,
  private val changeNoteArchiveStateUseCase: ChangeNoteArchiveStateUseCase,
  private val saveNoteUseCase: SaveNoteUseCase,
  private val createSharedNoteFileUseCase: CreateSharedNoteFileUseCase,
  private val imagesSingleton: ImagesSingleton,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  private val _notesScreenState = MutableStateFlow(
    NotesScreenState(
      isArchived = isArchived,
      isGrid = notePreferences.isNotesGridEnabled,
      sortOrder = notePreferences.noteOrder,
    ),
  )
  val notesScreenState = _notesScreenState.stateInWhileSubscribed(
    NotesScreenState(
      isArchived = isArchived,
      isGrid = notePreferences.isNotesGridEnabled,
      sortOrder = notePreferences.noteOrder,
    )
  ).onStart { refresh() }

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQuery = MutableStateFlow("")
  private val sortOrder = MutableStateFlow(notePreferences.noteOrder)
  private val refreshSignal = MutableStateFlow(0)

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTES_LIST))
    viewModelScope.launch(dispatcherProvider.default()) {
      combine(
        searchQuery.debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        sortOrder,
        refreshSignal,
      ) { query, order, _ -> query to order }
        .flatMapLatest { (query, order) ->
          flow {
            emit(
              noteRepository.getNotes(
                isArchived = isArchived,
                query = query.lowercase(),
                sortOrder = order
              )
            )
          }
        }.collect { applyList(it) }
    }
  }

  private fun refresh() {
    refreshSignal.update { it + 1 }
  }

  private fun applyList(list: List<NoteWithImages>) {
    val items = list.map { uiNoteListItemAdapter.convert(it) }
    _notesScreenState.update {
      it.copy(listState = if (items.isEmpty()) ListState.Empty else ListState.Ready(items))
    }
  }

  fun onSearchQueryChange(query: String) {
    _notesScreenState.update { it.copy(searchQuery = query) }
    searchQuery.value = query
  }

  fun onSortOrderSelected(order: String) {
    notePreferences.noteOrder = order
    _notesScreenState.update { it.copy(sortOrder = order) }
    sortOrder.value = order
  }

  fun onGridToggleClick() {
    val newValue = !_notesScreenState.value.isGrid
    notePreferences.isNotesGridEnabled = newValue
    _notesScreenState.update { it.copy(isGrid = newValue) }
  }

  fun onAddClick() {
    navigationEvent.emit(NavigationEvent.OpenCreateNote)
  }

  fun onArchiveClick() {
    navigationEvent.emit(NavigationEvent.OpenArchive)
  }

  fun onSettingsClick() {
    navigationEvent.emit(
      NavigationEvent.OpenSettings(textProvider.getString(R.string.action_settings))
    )
  }

  fun onNoteClick(id: String) {
    navigationEvent.emit(NavigationEvent.OpenNotePreview(id))
  }

  fun onImageClick(
    note: UiNoteListItem,
    imageId: Int,
  ) {
    val imagePosition = note.images.indexOfFirst { it.id == imageId }.takeIf { it != -1 } ?: 0
    imagesSingleton.setCurrent(
      images = note.images,
      backgroundColor = note.backgroundColor,
    )
    navigationEvent.emit(NavigationEvent.OpenImagePreview(note.id, imagePosition))
  }

  fun onNoteMenuAction(
    note: UiNoteListItem,
    action: NoteMenuAction,
  ) {
    when (action) {
      NoteMenuAction.OPEN -> onNoteClick(note.id)
      NoteMenuAction.EDIT -> navigationEvent.emit(NavigationEvent.OpenEditNote(note.id))
      NoteMenuAction.SHARE -> shareNote(note.id)
      NoteMenuAction.SHOW_IN_STATUS_BAR -> {
        navigationEvent.emit(NavigationEvent.RequestNotificationPermission(note.id))
      }

      NoteMenuAction.ARCHIVE -> moveToArchive(note.id)
      NoteMenuAction.UNARCHIVE -> unarchive(note.id)
      NoteMenuAction.DELETE -> navigationEvent.emit(NavigationEvent.ConfirmDelete(note.id))
    }
  }

  private fun moveToArchive(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      changeNoteArchiveStateUseCase(id, true)

      refresh()

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  private fun unarchive(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      changeNoteArchiveStateUseCase(id, false)
      refresh()
    }
  }

  private fun shareNote(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = noteRepository.getById(id)
      if (note == null) {
        withContext(dispatcherProvider.main()) {
          navigationEvent.emit(NavigationEvent.Error(textProvider.getText(R.string.failed_to_send_note)))
        }
        return@launch
      }
      val file = createSharedNoteFileUseCase(note)
      if (file == null) {
        withContext(dispatcherProvider.main()) {
          navigationEvent.emit(NavigationEvent.Error(textProvider.getText(R.string.failed_to_send_note)))
        }
        return@launch
      }
      if (!file.exists() || !file.canRead()) {
        withContext(dispatcherProvider.main()) {
          navigationEvent.emit(NavigationEvent.Error(textProvider.getText(R.string.error_sending)))
        }
        return@launch
      }
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.ShareNote(file, note.note?.summary))
      }
    }
  }

  fun deleteNote(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(id)

      refresh()

      if (!isArchived) {
        withContext(dispatcherProvider.main()) {
          appWidgetUpdater.updateNotesWidget()
        }
      }
    }
  }

  fun saveNoteColor(
    id: String,
    color: Int,
  ) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id) ?: return@launch
      val note = noteWithImages.note ?: return@launch
      note.color = color
      note.updatedAt = DateTimeManager.gmtDateTime
      saveNoteUseCase(noteWithImages)

      refresh()

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  fun showNoteInNotification(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id) ?: return@launch
      uiNoteNotificationAdapter.convert(noteWithImages).also {
        withContext(dispatcherProvider.main()) {
          noteNotifier.showNoteNotification(text = it.text, image = it.image, uniqueId = it.uniqueId)
        }
      }
    }
  }

  sealed interface NavigationEvent {
    data class OpenNotePreview(
      val id: String,
    ) : NavigationEvent

    data object OpenCreateNote : NavigationEvent

    data class OpenEditNote(
      val id: String,
    ) : NavigationEvent

    data object OpenArchive : NavigationEvent

    data class OpenSettings(
      val title: String,
    ) : NavigationEvent

    data class OpenImagePreview(
      val noteId: String,
      val imagePosition: Int,
    ) : NavigationEvent

    data class ShareNote(
      val file: File,
      val summary: String?,
    ) : NavigationEvent

    data class RequestNotificationPermission(
      val id: String,
    ) : NavigationEvent

    data class ConfirmDelete(
      val id: String,
    ) : NavigationEvent

    data class Error(val message: String) : NavigationEvent
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MS = 150L
  }
}
