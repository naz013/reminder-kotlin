package com.elementary.tasks.reminder.lists.removed

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.filter.query.ReminderQueryFilterInstance
import com.elementary.tasks.reminder.usecase.DeleteAllReminderUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Replaces `RemindersArchiveFragmentViewModel`: same read/filter/delete logic against removed
 * [Reminder]s, restructured around a single [StateFlow]<[RemindersArchiveScreenState]> and a
 * one-shot [NavigationEvent] channel, mirroring [com.elementary.tasks.home.eventsview.EventsViewModel].
 */
@OptIn(FlowPreview::class)
class RemindersArchiveViewModel(
  private val reminderRepository: ReminderRepository,
  dispatcherProvider: DispatcherProvider,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val deleteAllReminderUseCase: DeleteAllReminderUseCase,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<RemindersArchiveScreenState> field = MutableStateFlow(RemindersArchiveScreenState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQueryFlow = MutableStateFlow("")
  private var lastQuery = ""
  private var reminders = listOf<Reminder>()

  /** The [Reminder]s backing the currently displayed (search + group filtered) list — what
   *  "delete all" actually deletes, matching the legacy behaviour of only clearing what's visible. */
  private var filteredReminders = listOf<Reminder>()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      searchQueryFlow
        .debounce(SEARCH_DEBOUNCE_MS)
        .collect {
          lastQuery = it
          filterReminders()
        }
    }
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadReminders()
  }

  fun onSearchQueryChange(query: String) {
    state.update { it.copy(searchQuery = query) }
    searchQueryFlow.value = query
  }

  fun onItemClick(item: UiReminderList) {
    navigationEvent.value = Event(NavigationEvent.OpenEdit(item.id))
  }

  fun onMenuAction(
    item: UiReminderList,
    action: ArchiveReminderMenuAction,
  ) {
    when (action) {
      ArchiveReminderMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenEdit(item.id))
      ArchiveReminderMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDeleteReminder(item.id))
    }
  }

  fun onDeleteAllClick() {
    navigationEvent.value = Event(NavigationEvent.ConfirmDeleteAll)
  }

  fun deleteReminder(id: String) {
    Logger.i(TAG, "Deleting reminder: $id")
    withResultSuspend {
      reminderRepository.getById(id)?.let {
        deleteReminderUseCase(it)
        loadReminders()
        Commands.DELETED
      } ?: Commands.FAILED
    }
  }

  fun deleteAll() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val toDelete = filteredReminders
      if (toDelete.isEmpty()) return@launch
      Logger.i(TAG, "Deleting all reminders: ${toDelete.size}")
      deleteAllReminderUseCase(toDelete)
      loadReminders()
      navigationEvent.postValue(Event(NavigationEvent.ArchiveEmptied))
    }
  }

  private fun loadReminders() {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminders = reminderRepository.getByRemovedStatus(removed = true)
      filterReminders()
      Logger.i(TAG, "Loaded ${reminders.size} archived reminders.")
    }
  }

  private suspend fun filterReminders() {
    val filtered = filterByQuery(reminders = reminders, query = lastQuery)
    filteredReminders = filtered
    val uiItems = filtered.map { uiReminderListAdapter.create(it) }
    withContext(dispatcherProvider.main()) {
      state.update {
        it.copy(listState = if (uiItems.isEmpty()) ListState.Empty else ListState.Ready(uiItems))
      }
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
    private const val SEARCH_DEBOUNCE_MS = 300L
  }
}
