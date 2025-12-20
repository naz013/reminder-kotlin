package com.elementary.tasks.reminder.lists.removed

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.filter.AppliedFilters
import com.elementary.tasks.reminder.lists.filter.FilterGroup
import com.elementary.tasks.reminder.lists.filter.Filters
import com.elementary.tasks.reminder.lists.filter.ReminderGroupAppliedFilter
import com.elementary.tasks.reminder.lists.filter.ReminderGroupFilter
import com.elementary.tasks.reminder.lists.filter.ReminderGroupFilterGroup
import com.elementary.tasks.reminder.lists.filter.group.ReminderGroupFilterInstance
import com.elementary.tasks.reminder.lists.filter.query.ReminderQueryFilterInstance
import com.elementary.tasks.reminder.usecase.DeleteAllReminderUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class)
class RemindersArchiveFragmentViewModel(
  private val reminderRepository: ReminderRepository,
  dispatcherProvider: DispatcherProvider,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val textProvider: TextProvider,
  private val groupRepository: ReminderGroupRepository,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val deleteAllReminderUseCase: DeleteAllReminderUseCase,
) : BaseProgressViewModel(dispatcherProvider) {

  private val _events = mutableLiveDataOf<List<UiReminderEventsList>>()
  val events = _events.toLiveData()

  private val _showFilters = mutableLiveEventOf<Filters>()
  val showFilters = _showFilters.toLiveData()

  private val _canFilter = mutableLiveDataOf<Boolean>()
  val canFilter = _canFilter.toLiveData()

  private val _canSearch = mutableLiveDataOf<Boolean>()
  val canSearch = _canSearch.toLiveData()

  private val _canDeleteAll = mutableLiveDataOf<Boolean>()
  val canDeleteAll = _canDeleteAll.toLiveData()

  private var appliedFilters: AppliedFilters = AppliedFilters()
  private var lastQuery: String = ""
  private var allFilters = listOf<FilterGroup>()
  private var reminders = listOf<Reminder>()

  private val queryFilterFlow = MutableStateFlow("")

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      queryFilterFlow
        .debounce(300)
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

  fun showFilters() {
    if (allFilters.isEmpty()) {
      Logger.w(TAG, "No filters to show.")
      return
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      val currentSelected = appliedFilters.selectedFilters
      val filters = allFilters.map { group ->
        val appliedFilter = currentSelected[group.id]
        when (group) {
          is ReminderGroupFilterGroup -> {
            ReminderGroupFilterGroup(
              id = group.id,
              title = group.title,
              appliedFilter = appliedFilter as? ReminderGroupAppliedFilter,
              filters = group.filters
            )
          }

          else -> group
        }
      }
      withContext(dispatcherProvider.main()) {
        Logger.i(TAG, "Showing filters: ${filters.size}")
        _showFilters.value = Event(Filters(filters))
      }
    }
  }

  fun handleFilterResult(appliedFilters: AppliedFilters?) {
    this.appliedFilters = appliedFilters ?: AppliedFilters()
    Logger.i(TAG, "Applied filters: $appliedFilters")
    viewModelScope.launch(dispatcherProvider.main()) {
      filterReminders()
    }
  }

  fun onSearchUpdate(query: String) {
    Logger.i(TAG, "On search update: ${Logger.private(query)}")
    queryFilterFlow.tryEmit(query)
  }

  fun deleteReminder(id: String) {
    Logger.i(TAG, "Deleting reminder: $id")
    withResultSuspend {
      reminderRepository.getById(id)?.let {
        deleteReminderUseCase(it)
        loadReminders()
        Commands.DELETED
      } ?: run {
        Commands.FAILED
      }
    }
  }

  fun deleteAll() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminders = _events.value?.mapNotNull { uiReminderEventsList ->
        reminders.find { it.uuId == uiReminderEventsList.id}
      } ?: run {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      Logger.i(TAG, "Deleting all reminders: ${reminders.size}")
      deleteAllReminderUseCase(reminders)
      loadReminders()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun loadReminders() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminders = reminderRepository.getByRemovedStatus(removed = true)
      prepareFilters()
      filterReminders()
      postInProgress(false)
      Logger.i(TAG, "Loaded ${reminders.size} active reminders. can search: ${reminders.isNotEmpty()}")
      withContext(dispatcherProvider.main()) {
        _canSearch.value = reminders.isNotEmpty()
      }
    }
  }

  private suspend fun prepareFilters() {
    val filterGroups = mutableListOf<FilterGroup>()
    val groupFilters = groupRepository.getAll()
      .map { ReminderGroupFilter(it.groupUuId, it.groupTitle) }
    if (groupFilters.isNotEmpty()) {
      filterGroups.add(
        ReminderGroupFilterGroup(
          id = GROUP_FILTER_ID,
          title = textProvider.getString(R.string.groups),
          appliedFilter = null,
          filters = groupFilters,
        )
      )
    }
    allFilters = filterGroups
    withContext(dispatcherProvider.main()) {
      _canFilter.value = filterGroups.isNotEmpty() && reminders.isNotEmpty()
    }
  }

  private suspend fun filterReminders() {
    val filtered = filterByGroups(
      reminders = filterByQuery(
        reminders = reminders,
        query = lastQuery
      ),
      groupIds = (appliedFilters.selectedFilters[GROUP_FILTER_ID] as? ReminderGroupAppliedFilter)?.selectedFilterIds
    )
    val uiLists = filtered.map { uiReminderListAdapter.create(it) }
    withContext(dispatcherProvider.main()) {
      _events.value = uiLists
      _canDeleteAll.value = uiLists.isNotEmpty()
    }
  }

  private fun filterByGroups(reminders: List<Reminder>, groupIds: Set<String>?): List<Reminder> {
    if (groupIds.isNullOrEmpty()) return reminders
    return reminders.filter(ReminderGroupFilterInstance(groupIds)).also {
      Logger.i(
        TAG,
        "Filtered by groups: ${it.size} items left, was: ${reminders.size}. Groups: ${groupIds.joinToString()}"
      )
    }
  }

  private fun filterByQuery(reminders: List<Reminder>, query: String): List<Reminder> {
    if (query.isBlank()) return reminders
    return reminders.filter(ReminderQueryFilterInstance(lastQuery)).also {
      Logger.i(
        TAG,
        "Filtered by query: ${it.size} items left, was: ${reminders.size}. Query: ${
          Logger.private(query)
        }"
      )
    }
  }

  companion object {
    private const val TAG = "ArchiveRemindersViewModel"
    private const val GROUP_FILTER_ID = "groups"
  }
}
