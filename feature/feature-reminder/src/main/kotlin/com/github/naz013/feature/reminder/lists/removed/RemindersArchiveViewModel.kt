package com.github.naz013.feature.reminder.lists.removed

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.reminder.UiReminderListAdapter
import com.github.naz013.logic.reminder.filter.ReminderV2QueryFilterInstance
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.usecase.DeleteAllReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class RemindersArchiveViewModel(
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val dispatcherProvider: DispatcherProvider,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val deleteAllReminderUseCase: DeleteAllReminderUseCase,
) : ViewModel() {

  private val _state = MutableStateFlow(RemindersArchiveScreenState())
  private val _selectedItemId = MutableStateFlow<String?>(null)
  val state = combine(_state, _selectedItemId, RemindersArchiveScreenState::withSelectedItem)
    .stateInWhileSubscribed(RemindersArchiveScreenState())
    .onStart { loadReminders() }

  val event: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQueryFlow = MutableStateFlow("")

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      searchQueryFlow
        .debounce(SEARCH_DEBOUNCE_MS)
        .collect { filterReminders() }
    }
  }

  fun onSearchQueryChange(query: String) {
    _state.update { it.copy(searchQuery = query) }
    searchQueryFlow.value = query
  }

  fun onSelectedItemIdChanged(id: String?) {
    _selectedItemId.value = id
  }

  fun onItemClick(item: UiReminderList) {
    event.emit(NavigationEvent.OpenEdit(item.id))
  }

  fun onMenuAction(
    item: UiReminderList,
    action: ArchiveReminderMenuAction,
  ) {
    when (action) {
      ArchiveReminderMenuAction.EDIT -> event.emit(NavigationEvent.OpenEdit(item.id))
      ArchiveReminderMenuAction.DELETE -> event.emit(NavigationEvent.ConfirmDeleteReminder(item.id))
    }
  }

  fun onDeleteAllClick() {
    event.emit(NavigationEvent.ConfirmDeleteAll)
  }

  fun deleteReminder(id: String) {
    Logger.i(TAG, "Deleting reminder: $id")
    viewModelScope.launch(dispatcherProvider.main()) {
      withContext(dispatcherProvider.io()) {
        reminderV2Repository.getById(id)?.also {
          deleteReminderUseCase(it)
        }
      } ?: run {
        Logger.e(TAG, "Cannot delete reminder with id = $id. Not found.")
        return@launch
      }

      loadReminders()
    }
  }

  fun deleteAll() {
    viewModelScope.launch(dispatcherProvider.main()) {
      val toDeleteIds = _state.value.filteredReminders.map { it.uuId }
      if (toDeleteIds.isEmpty()) return@launch
      Logger.i(TAG, "Deleting all reminders: ${toDeleteIds.size}")
      withContext(dispatcherProvider.io()) {
        val toDelete = toDeleteIds.mapNotNull { reminderV2Repository.getById(it) }
        deleteAllReminderUseCase(toDelete)
      }
      loadReminders()
      event.emit(NavigationEvent.ArchiveEmptied)
    }
  }

  private fun loadReminders() {
    viewModelScope.launch(dispatcherProvider.main()) {
      val allReminders = withContext(dispatcherProvider.io()) {
        reminderV2Repository.getByRemovedStatus(removed = true)
      }
      _state.update {
        it.copy(allReminders = allReminders)
      }
      filterReminders()
      Logger.i(TAG, "Loaded ${allReminders.size} archived reminders.")
    }
  }

  private suspend fun filterReminders() {
    val filtered = withContext(dispatcherProvider.default()) {
      filterByQuery(reminders = _state.value.allReminders, query = _state.value.searchQuery)
    }
    _state.update {
      it.copy(filteredReminders = filtered)
    }
    val uiItems = withContext(dispatcherProvider.default()) {
      val groupsById = groupV2Repository.getAll().associateBy { it.uuId }
      filtered.map { uiReminderListAdapter.createV2(it, it.groupId?.let { id -> groupsById[id] }) }
    }
    _state.update {
      it.copy(listState = if (uiItems.isEmpty()) ListState.Empty else ListState.Ready(uiItems))
    }
  }

  private fun filterByQuery(
    reminders: List<ReminderV2>,
    query: String,
  ): List<ReminderV2> {
    if (query.isBlank()) return reminders
    return reminders.filter(ReminderV2QueryFilterInstance(query)).also {
      Logger.i(
        TAG,
        "Filtered by query: ${it.size} items left, was: ${reminders.size}. Query: ${Logger.private(query)}",
      )
    }
  }

  sealed interface NavigationEvent {
    data class OpenEdit(
      val id: String,
    ) : NavigationEvent

    data class ConfirmDeleteReminder(
      val id: String,
    ) : NavigationEvent

    data object ConfirmDeleteAll : NavigationEvent

    data object ArchiveEmptied : NavigationEvent
  }

  companion object {
    private const val TAG = "RemindersArchiveViewModel"
    private val SEARCH_DEBOUNCE_MS = 300L.milliseconds
  }
}
