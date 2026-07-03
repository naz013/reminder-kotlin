package com.elementary.tasks.notes.list

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.usecase.ChangeNoteArchiveStateUseCase
import com.elementary.tasks.notes.usecase.CreateSharedNoteFileUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.notes.usecase.SaveNoteUseCase
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NotesViewModel(
  dispatcherProvider: DispatcherProvider,
  private val textProvider: TextProvider,
  private val uiNoteListItemAdapter: UiNoteListItemAdapter,
  private val prefs: Prefs,
  private val noteRepository: NoteRepository,
  private val uiNoteNotificationAdapter: UiNoteNotificationAdapter,
  private val notifier: Notifier,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val deleteNoteUseCase: DeleteNoteUseCase,
  private val changeNoteArchiveStateUseCase: ChangeNoteArchiveStateUseCase,
  private val saveNoteUseCase: SaveNoteUseCase,
  private val createSharedNoteFileUseCase: CreateSharedNoteFileUseCase,
  private val imagesSingleton: ImagesSingleton,
  private val isArchived: Boolean = false
) : BaseProgressViewModel(dispatcherProvider) {

  val notesScreenState: StateFlow<NotesScreenState> field = MutableStateFlow(
    NotesScreenState(
      isArchived = isArchived,
      isGrid = prefs.isNotesGridEnabled,
      sortOrder = prefs.noteOrder
    )
  )
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val noteSortProcessor = NoteSortProcessor()
  private val notesData = SearchableNotesData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    noteRepository = noteRepository,
    isArchived = isArchived
  )
  private val notesObserver = Observer<List<NoteWithImages>> { applyList(it) }

  init {
    notesData.observeForever(notesObserver)
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    notesData.refresh()
  }

  override fun onCleared() {
    super.onCleared()
    notesData.removeObserver(notesObserver)
  }

  private fun applyList(list: List<NoteWithImages>) {
    val items = noteSortProcessor.apply(
      list.map { uiNoteListItemAdapter.convert(it) },
      notesScreenState.value.sortOrder
    )
    notesScreenState.update {
      it.copy(listState = if (items.isEmpty()) ListState.Empty else ListState.Ready(items))
    }
  }

  fun onSearchQueryChange(query: String) {
    notesScreenState.update { it.copy(searchQuery = query) }
    notesData.onNewQuery(query)
  }

  fun onSortOrderSelected(order: String) {
    prefs.noteOrder = order
    notesScreenState.update { it.copy(sortOrder = order) }
    notesData.refresh()
  }

  fun onGridToggleClick() {
    val newValue = !notesScreenState.value.isGrid
    prefs.isNotesGridEnabled = newValue
    notesScreenState.update { it.copy(isGrid = newValue) }
  }

  fun onAddClick() {
    navigationEvent.value = Event(NavigationEvent.OpenCreateNote)
  }

  fun onArchiveClick() {
    navigationEvent.value = Event(NavigationEvent.OpenArchive)
  }

  fun onSettingsClick() {
    navigationEvent.value = Event(NavigationEvent.OpenSettings)
  }

  fun onNoteClick(id: String) {
    navigationEvent.value = Event(NavigationEvent.OpenNotePreview(id))
  }

  fun onImageClick(note: UiNoteListItem, imageId: Int) {
    val imagePosition = note.images.indexOfFirst { it.id == imageId }.takeIf { it != -1 } ?: 0
    imagesSingleton.setCurrent(
      images = note.images,
      color = note.colorPosition,
      palette = note.colorPalette
    )
    navigationEvent.value = Event(NavigationEvent.OpenImagePreview(note.id, imagePosition))
  }

  fun onNoteMenuAction(note: UiNoteListItem, action: NoteMenuAction) {
    when (action) {
      NoteMenuAction.OPEN -> onNoteClick(note.id)
      NoteMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenEditNote(note.id))
      NoteMenuAction.SHARE -> shareNote(note.id)
      NoteMenuAction.SHOW_IN_STATUS_BAR -> {
        navigationEvent.value = Event(NavigationEvent.RequestNotificationPermission(note.id))
      }

      NoteMenuAction.CHANGE_COLOR -> {
        navigationEvent.value = Event(
          NavigationEvent.PickColor(note.id, note.colorPosition, note.colorPalette)
        )
      }

      NoteMenuAction.ARCHIVE -> moveToArchive(note.id)
      NoteMenuAction.UNARCHIVE -> unarchive(note.id)
      NoteMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDelete(note.id))
    }
  }

  private fun moveToArchive(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      changeNoteArchiveStateUseCase(id, true)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.UPDATED)

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  private fun unarchive(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      changeNoteArchiveStateUseCase(id, false)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }

  private fun shareNote(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = noteRepository.getById(id)
      if (note == null) {
        postInProgress(false)
        postError(textProvider.getText(R.string.failed_to_send_note))
        return@launch
      }
      val file = createSharedNoteFileUseCase(note)
      postInProgress(false)
      if (file == null) {
        postError(textProvider.getText(R.string.failed_to_send_note))
        return@launch
      }
      if (!file.exists() || !file.canRead()) {
        postError(textProvider.getText(R.string.error_sending))
        return@launch
      }
      withContext(dispatcherProvider.main()) {
        navigationEvent.value = Event(NavigationEvent.ShareNote(file, note.note?.summary))
      }
    }
  }

  fun deleteNote(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(id)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.DELETED)

      if (!isArchived) {
        withContext(dispatcherProvider.main()) {
          appWidgetUpdater.updateNotesWidget()
        }
      }
    }
  }

  fun saveNoteColor(id: String, color: Int) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id)
      if (noteWithImages == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      val note = noteWithImages.note
      if (note == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      note.color = color
      note.updatedAt = DateTimeManager.gmtDateTime
      saveNoteUseCase(noteWithImages)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.SAVED)

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  fun showNoteInNotification(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id) ?: return@launch
      uiNoteNotificationAdapter.convert(noteWithImages).also {
        withContext(dispatcherProvider.main()) { notifier.showNoteNotification(it) }
      }
    }
  }

  sealed interface NavigationEvent {
    data class OpenNotePreview(val id: String) : NavigationEvent
    data object OpenCreateNote : NavigationEvent
    data class OpenEditNote(val id: String) : NavigationEvent
    data object OpenArchive : NavigationEvent
    data object OpenSettings : NavigationEvent
    data class OpenImagePreview(val noteId: String, val imagePosition: Int) : NavigationEvent
    data class ShareNote(val file: File, val summary: String?) : NavigationEvent
    data class RequestNotificationPermission(val id: String) : NavigationEvent
    data class PickColor(val id: String, val colorPosition: Int, val colorPalette: Int) : NavigationEvent
    data class ConfirmDelete(val id: String) : NavigationEvent
  }
}
