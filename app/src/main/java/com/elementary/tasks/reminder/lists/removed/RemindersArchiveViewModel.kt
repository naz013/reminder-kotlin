package com.elementary.tasks.reminder.lists.removed

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.filter.query.ReminderQueryFilterInstance
import com.elementary.tasks.reminder.usecase.DeleteAllReminderUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class RemindersArchiveViewModel(
  private val reminderRepository: ReminderRepository,
  private val dispatcherProvider: DispatcherProvider,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val deleteAllReminderUseCase: DeleteAllReminderUseCase,
) : ViewModel() {

  private val _state = MutableStateFlow(RemindersArchiveScreenState())
  val state = _state.stateInWhileSubscribed(RemindersArchiveScreenState())
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
        reminderRepository.getById(id)?.also {
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
      val toDelete = _state.value.filteredReminders
      if (toDelete.isEmpty()) return@launch
      Logger.i(TAG, "Deleting all reminders: ${toDelete.size}")
      withContext(dispatcherProvider.io()) {
        deleteAllReminderUseCase(toDelete)
      }
      loadReminders()
      event.emit(NavigationEvent.ArchiveEmptied)
    }
  }

  private fun loadReminders() {
    viewModelScope.launch(dispatcherProvider.main()) {
      val allReminders = withContext(dispatcherProvider.io()) {
        reminderRepository.getByRemovedStatus(removed = true)
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
      filtered.map { uiReminderListAdapter.create(it) }
    }
    _state.update {
      it.copy(listState = if (uiItems.isEmpty()) ListState.Empty else ListState.Ready(uiItems))
    }
  }

  private fun filterByQuery(
    reminders: List<Reminder>,
    query: String,
  ): List<Reminder> {
    if (query.isBlank()) return reminders
    return reminders.filter(ReminderQueryFilterInstance(query)).also {
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
