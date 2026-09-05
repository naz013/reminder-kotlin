package com.github.naz013.feature.birthday.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.PendingDeleteTracker
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logic.birthday.BirthdayQueryFilter
import com.github.naz013.logic.birthday.BirthdaySmartListPredicate
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.agenda.AgendaMenuAction
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaItemAdapter
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.selection.clearSelection
import com.github.naz013.ui.common.selection.select
import com.github.naz013.ui.common.selection.selectedCount
import com.github.naz013.ui.common.selection.selectedIds
import com.github.naz013.ui.common.selection.toggleSelection
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
import org.threeten.bp.LocalDate
import java.util.UUID

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class BirthdaysViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val textProvider: TextProvider,
  private val birthdayRepository: BirthdayRepository,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
  private val uiAgendaItemAdapter: UiAgendaItemAdapter,
  private val birthdaySmartListPredicate: BirthdaySmartListPredicate,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
) : ViewModel() {

  private val _state = MutableStateFlow(BirthdaysScreenState())
  private val _selectedItemId = MutableStateFlow<String?>(null)
  val state = combine(_state, _selectedItemId, BirthdaysScreenState::withSelectedItem)
    .stateInWhileSubscribed(BirthdaysScreenState())

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQuery = MutableStateFlow("")
  private val selectedSmartList = MutableStateFlow<SmartListFilter?>(null)
  private val selectedTagId = MutableStateFlow<String?>(null)
  private val pendingDeleteTracker = PendingDeleteTracker()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tags -> _state.update { it.copy(availableTags = tags) } }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      combine(
        searchQuery.debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        selectedSmartList,
      ) { query, smartList -> query to smartList }
        .flatMapLatest { (query, smartList) ->
          birthdayRepository.observeAll().map { all -> filterBirthdays(all, query, smartList) }
        }
        .combine(selectedTagId) { birthdays, tagId -> birthdays to tagId }
        .map { (birthdays, tagId) ->
          if (tagId == null) {
            birthdays
          } else {
            val ids = tagAssignmentRepository.getItemIdsForTag(tagId, TaggedItemType.BIRTHDAY).toSet()
            birthdays.filter { it.uuId in ids }
          }
        }
        .combine(pendingDeleteTracker.pendingIds) { birthdays, pendingIds ->
          birthdays.filterNot { it.uuId in pendingIds }
        }
        .collect { applyList(it) }
    }
  }

  private fun filterBirthdays(
    all: List<Birthday>,
    query: String,
    smartList: SmartListFilter?,
  ): List<Birthday> {
    val byQuery = if (query.isBlank()) all else all.filter { BirthdayQueryFilter(query)(it) }
    return if (smartList == null) {
      byQuery
    } else {
      val today = LocalDate.now()
      byQuery.filter { birthdaySmartListPredicate.matches(smartList, it, today) }
    }
  }

  private fun applyList(list: List<Birthday>) {
    val items = list.map { uiAgendaItemAdapter.convertBirthday(it) }.sortedBy { it.dateTime }
    _state.update {
      it.copy(
        listState = if (items.isEmpty()) ListState.Empty else ListState.Ready(items),
        hasAnyItems = list.isNotEmpty() || it.searchQuery.isNotEmpty() || it.selectedSmartList != null || it.selectedTagId != null,
      )
    }
  }

  fun onSearchQueryChange(query: String) {
    _state.update { it.copy(searchQuery = query) }
    searchQuery.value = query
  }

  fun onSmartListSelected(filter: SmartListFilter?) {
    val updated = if (selectedSmartList.value == filter) null else filter
    selectedSmartList.value = updated
    _state.update { it.copy(selectedSmartList = updated) }
  }

  fun onTagFilterSelected(tagId: String?) {
    val updated = if (tagId != null && tagId == selectedTagId.value) null else tagId
    selectedTagId.value = updated
    _state.update { it.copy(selectedTagId = updated) }
  }

  fun onSelectedItemIdChanged(id: String?) {
    _selectedItemId.value = id
  }

  fun onItemClick(item: UiAgendaBirthday) {
    if (_state.value.selectedCount > 0) {
      updateSelection { it.toggleSelection(item.id) }
    } else {
      navigationEvent.emit(NavigationEvent.OpenPreview(item.id))
    }
  }

  fun onItemLongClick(id: String) {
    updateSelection { it.select(id) }
  }

  fun onSelectionCancel() {
    updateSelection { it.clearSelection() }
  }

  private fun updateSelection(transform: (List<UiAgendaBirthday>) -> List<UiAgendaBirthday>) {
    _state.update { state ->
      val listState = state.listState
      if (listState !is ListState.Ready) return@update state
      val items = transform(listState.items)
      state.copy(listState = ListState.Ready(items), selectedCount = items.selectedCount())
    }
  }

  private fun selectedIds(): Set<String> =
    (_state.value.listState as? ListState.Ready)?.items.orEmpty().selectedIds()

  fun onDeleteSelectedClick() {
    val ids = selectedIds()
    if (ids.isEmpty()) return
    navigationEvent.emit(
      NavigationEvent.ConfirmDeleteSelected(
        ids = ids,
        title = textProvider.getText(R.string.birthdays_delete_selected_permanently, ids.size),
      ),
    )
  }

  fun deleteSelectedBirthdays(ids: Set<String>) {
    if (ids.isEmpty()) return
    val batchKey = UUID.randomUUID().toString()
    pendingDeleteTracker.markPending(batchKey = batchKey, ids = ids) {
      ids.forEach { deleteBirthdayUseCase(it) }
    }
    onSelectionCancel()
    navigationEvent.emit(
      NavigationEvent.ShowUndoDelete(
        batchKey = batchKey,
        message = textProvider.getText(R.string.birthdays_deleted_count, ids.size),
      )
    )
  }

  fun onMenuAction(
    item: UiAgendaBirthday,
    action: AgendaMenuAction,
  ) {
    when (action) {
      AgendaMenuAction.OPEN -> navigationEvent.emit(NavigationEvent.OpenPreview(item.id))
      AgendaMenuAction.EDIT -> navigationEvent.emit(NavigationEvent.OpenEdit(item.id))
      AgendaMenuAction.DELETE -> _state.update { it.copy(confirmDeleteId = item.id) }
      AgendaMenuAction.ARCHIVE, AgendaMenuAction.SKIP, AgendaMenuAction.TURN_OFF,
      AgendaMenuAction.PIN, AgendaMenuAction.UNPIN,
      -> Unit
    }
  }

  fun onAddClick() {
    navigationEvent.emit(NavigationEvent.OpenNewBirthday)
  }

  fun onDeleteDismiss() {
    _state.update { it.copy(confirmDeleteId = null) }
  }

  fun onDeleteConfirmed() {
    val id = _state.value.confirmDeleteId ?: return
    _state.update { it.copy(confirmDeleteId = null) }
    pendingDeleteTracker.markPending(batchKey = id, ids = setOf(id)) {
      deleteBirthdayUseCase(id)
    }
    navigationEvent.emit(
      NavigationEvent.ShowUndoDelete(batchKey = id, message = textProvider.getText(R.string.birthday_deleted))
    )
  }

  fun undoDelete(batchKey: String) {
    pendingDeleteTracker.undo(batchKey)
  }

  fun commitDelete(batchKey: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      pendingDeleteTracker.commit(batchKey)
    }
  }

  sealed interface NavigationEvent {
    data class OpenPreview(val id: String) : NavigationEvent
    data class OpenEdit(val id: String) : NavigationEvent
    data object OpenNewBirthday : NavigationEvent
    data class ConfirmDeleteSelected(val ids: Set<String>, val title: String) : NavigationEvent
    data class ShowUndoDelete(val batchKey: String, val message: String) : NavigationEvent
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MS = 300L
  }
}
