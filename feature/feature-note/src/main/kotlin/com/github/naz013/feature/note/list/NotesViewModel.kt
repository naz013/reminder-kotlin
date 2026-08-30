package com.github.naz013.feature.note.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.UiNoteNotificationAdapter
import com.github.naz013.feature.note.preview.ImagesSingleton
import com.github.naz013.feature.note.usecase.ChangeNoteArchiveStateUseCase
import com.github.naz013.feature.note.usecase.CreateSharedNoteFileUseCase
import com.github.naz013.feature.note.usecase.DeleteNoteUseCase
import com.github.naz013.feature.note.usecase.MergeNotesUseCase
import com.github.naz013.feature.note.usecase.SaveNoteUseCase
import com.github.naz013.feature.note.usecase.TogglePinnedNoteUseCase
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.common.selection.clearSelection
import com.github.naz013.ui.common.selection.select
import com.github.naz013.ui.common.selection.selectedCount
import com.github.naz013.ui.common.selection.selectedIds
import com.github.naz013.ui.common.selection.toggleSelection
import com.github.naz013.ui.note.ListLayoutMode
import com.github.naz013.ui.note.NoteColorEngine
import com.github.naz013.ui.note.NoteNotifier
import com.github.naz013.ui.note.NotePreferences
import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.note.UiNoteListItemAdapter
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class NotesViewModel(
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
  private val mergeNotesUseCase: MergeNotesUseCase,
  private val changeNoteArchiveStateUseCase: ChangeNoteArchiveStateUseCase,
  private val togglePinnedNoteUseCase: TogglePinnedNoteUseCase,
  private val saveNoteUseCase: SaveNoteUseCase,
  private val createSharedNoteFileUseCase: CreateSharedNoteFileUseCase,
  private val imagesSingleton: ImagesSingleton,
  private val analyticsEventSender: AnalyticsEventSender,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
  private val noteColorEngine: NoteColorEngine,
) : ViewModel() {

  private val _notesScreenState = MutableStateFlow(
    NotesScreenState(
      isArchived = isArchived,
      layoutMode = notePreferences.notesLayoutMode,
      sortOrder = notePreferences.noteOrder,
    ),
  )
  val notesScreenState = _notesScreenState.stateInWhileSubscribed(
    NotesScreenState(
      isArchived = isArchived,
      layoutMode = notePreferences.notesLayoutMode,
      sortOrder = notePreferences.noteOrder,
    )
  )

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQuery = MutableStateFlow("")
  private val sortOrder = MutableStateFlow(notePreferences.noteOrder)
  private val selectedTagId = MutableStateFlow<String?>(null)

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTES_LIST))
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tags ->
          _notesScreenState.update { it.copy(allTags = tags) }
        }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      combine(
        searchQuery.debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        sortOrder,
      ) { query, order -> query to order }
        .flatMapLatest { (query, order) ->
          noteRepository.observeNotes(isArchived = isArchived, query = query.lowercase(), sortOrder = order)
        }
        .combine(selectedTagId) { notes, tagId -> notes to tagId }
        .map { (notes, tagId) ->
          if (tagId == null) {
            notes
          } else {
            val ids = tagAssignmentRepository.getItemIdsForTag(tagId, TaggedItemType.NOTE).toSet()
            notes.filter { it.note?.key in ids }
          }
        }
        .collect { applyList(it) }
    }
  }

  fun onTagSelected(tagId: String?) {
    val newSelectedTagId = if (tagId != null && tagId == selectedTagId.value) null else tagId
    _notesScreenState.update { it.copy(selectedTagId = newSelectedTagId) }
    selectedTagId.value = newSelectedTagId
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

  fun onLayoutModeSelected(mode: ListLayoutMode) {
    notePreferences.notesLayoutMode = mode
    _notesScreenState.update { it.copy(layoutMode = mode) }
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

  // Tap order for selected notes - used only by merge, which needs to know which note was
  // selected *first* to decide the merged note's title/appearance. Kept out of NotesScreenState
  // on purpose since no other bulk action needs it (see docs/multiselect.md).
  private var selectionOrder: List<String> = emptyList()

  fun onNoteClick(id: String) {
    if (_notesScreenState.value.selectedCount > 0) {
      updateSelection { it.toggleSelection(id) }
      selectionOrder = if (id in selectionOrder) selectionOrder - id else selectionOrder + id
    } else {
      navigationEvent.emit(NavigationEvent.OpenNotePreview(id))
    }
  }

  fun onNoteLongClick(id: String) {
    updateSelection { it.select(id) }
    if (id !in selectionOrder) selectionOrder = selectionOrder + id
  }

  fun onSelectionCancel() {
    updateSelection { it.clearSelection() }
    selectionOrder = emptyList()
  }

  private fun updateSelection(transform: (List<UiNoteListItem>) -> List<UiNoteListItem>) {
    _notesScreenState.update { state ->
      val listState = state.listState
      if (listState !is ListState.Ready) return@update state
      val notes = transform(listState.notes)
      state.copy(listState = ListState.Ready(notes), selectedCount = notes.selectedCount())
    }
  }

  private fun selectedIds(): Set<String> =
    (_notesScreenState.value.listState as? ListState.Ready)?.notes.orEmpty().selectedIds()

  private fun selectedIdsInOrder(): List<String> {
    val selected = selectedIds()
    return selectionOrder.filter { it in selected }
  }

  fun onDeleteSelectedClick() {
    val ids = selectedIds()
    if (ids.isNotEmpty()) {
      navigationEvent.emit(
        NavigationEvent.ConfirmDeleteSelected(
          ids = ids,
          title = textProvider.getText(R.string.notes_delete_selected_permanently, ids.size),
        )
      )
    }
  }

  fun deleteSelectedNotes(ids: Set<String>) {
    viewModelScope.launch(dispatcherProvider.default()) {
      ids.forEach { deleteNoteUseCase(it) }

      withContext(dispatcherProvider.main()) {
        onSelectionCancel()
      }

      if (!isArchived) {
        withContext(dispatcherProvider.main()) {
          appWidgetUpdater.updateNotesWidget()
        }
      }
    }
  }

  fun onArchiveSelectedClick() {
    val ids = selectedIds()
    if (ids.isEmpty()) return
    viewModelScope.launch(dispatcherProvider.default()) {
      ids.forEach { changeNoteArchiveStateUseCase(it, !isArchived) }

      withContext(dispatcherProvider.main()) {
        onSelectionCancel()
      }

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  fun applySelectedColor(colorIndex: Int) {
    val ids = selectedIds()
    if (ids.isEmpty()) return
    viewModelScope.launch(dispatcherProvider.default()) {
      ids.forEach { id ->
        val noteWithImages = noteRepository.getById(id) ?: return@forEach
        val note = noteWithImages.note ?: return@forEach
        note.color = colorIndex
        note.updatedAt = DateTimeManager.gmtDateTime
        saveNoteUseCase(noteWithImages)
      }

      withContext(dispatcherProvider.main()) {
        onSelectionCancel()
      }

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  fun onMergeSelectedClick() {
    val ids = selectedIdsInOrder()
    if (ids.size < 2) return
    navigationEvent.emit(
      NavigationEvent.ConfirmMergeSelected(
        ids = ids,
        title = textProvider.getText(R.string.notes_merge_selected_confirm, ids.size),
      )
    )
  }

  fun mergeSelectedNotes(ids: List<String>) {
    viewModelScope.launch(dispatcherProvider.default()) {
      mergeNotesUseCase(ids)

      withContext(dispatcherProvider.main()) {
        onSelectionCancel()
      }

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
      }
    }
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
      NoteMenuAction.PIN, NoteMenuAction.UNPIN -> togglePinned(note.id)
      NoteMenuAction.DELETE -> navigationEvent.emit(NavigationEvent.ConfirmDelete(note.id))
    }
  }

  private fun togglePinned(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      togglePinnedNoteUseCase(id)
    }
  }

  private fun moveToArchive(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      changeNoteArchiveStateUseCase(id, true)

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  private fun unarchive(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      changeNoteArchiveStateUseCase(id, false)
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
        navigationEvent.emit(NavigationEvent.ShareNote(file, note.note?.content?.text))
      }
    }
  }

  fun deleteNote(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(id)

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

    data class ConfirmDeleteSelected(
      val ids: Set<String>,
      val title: String,
    ) : NavigationEvent

    data class ConfirmMergeSelected(
      val ids: List<String>,
      val title: String,
    ) : NavigationEvent

    data class Error(val message: String) : NavigationEvent
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MS = 150L
  }
}
