package com.elementary.tasks.reminder.lists.todo

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.lists.active.ActiveRemindersViewModel
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.elementary.tasks.reminder.lists.data.UiReminderListsAdapter
import com.elementary.tasks.reminder.lists.filter.AppliedFilters
import com.elementary.tasks.reminder.lists.filter.Filter
import com.elementary.tasks.reminder.lists.filter.FilterGroup
import com.elementary.tasks.reminder.lists.filter.group.ReminderGroupFilter
import com.elementary.tasks.reminder.lists.filter.query.ReminderQueryFilter
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.IntentKeys
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
class ActiveTodoRemindersViewModel(
  dispatcherProvider: DispatcherProvider,
  private val reminderRepository: ReminderRepository,
  private val eventControlFactory: EventControlFactory,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderListsAdapter: UiReminderListsAdapter,
  private val textProvider: TextProvider,
  private val groupRepository: ReminderGroupRepository
) : BaseProgressViewModel(dispatcherProvider) {

  private val _events = mutableLiveDataOf<List<UiReminderEventsList>>()
  val events = _events.toLiveData()

  private val _showFilters = mutableLiveEventOf<List<FilterGroup>>()
  val showFilters = _showFilters.toLiveData()

  private val _canFilter = mutableLiveDataOf<Boolean>()
  val canFilter = _canFilter.toLiveData()

  private val _canSearch = mutableLiveDataOf<Boolean>()
  val canSearch = _canSearch.toLiveData()

  private var appliedFilters: AppliedFilters? = null
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
      return
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      val currentSelected = appliedFilters?.selectedFilters ?: emptyMap()
      val filters = allFilters.map { group ->
        val updatedFilters = group.filters.map { filter ->
          val isSelected = currentSelected[group.id]?.contains(filter.id) == true
          Filter(filter.id, filter.label, isSelected)
        }
        FilterGroup(group.id, group.title, updatedFilters)
      }
      withContext(dispatcherProvider.main()) {
        _showFilters.value = Event(filters)
      }
    }
  }

  fun handleFilterResult(appliedFilters: AppliedFilters?) {
    this.appliedFilters = appliedFilters
    Logger.i(TAG, "Applied filters: $appliedFilters")
    viewModelScope.launch(dispatcherProvider.main()) {
      filterReminders()
    }
  }

  fun onSearchUpdate(query: String) {
    Logger.i(TAG, "On search update: ${Logger.private(query)}")
    queryFilterFlow.tryEmit(query)
  }

  fun skip(id: String) {
    withResultSuspend {
      val fromDb = reminderRepository.getById(id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          fromDb.uuId
        )
        loadReminders()
        Commands.SAVED
      }
      Commands.FAILED
    }
  }

  fun toggleReminder(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val item = reminderRepository.getById(id) ?: return@launch
      if (!eventControlFactory.getController(item).onOff()) {
        postInProgress(false)
        postCommand(Commands.OUTDATED)
      } else {
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          item.uuId
        )
        postInProgress(false)
        postCommand(Commands.SAVED)
      }
      loadReminders()
    }
  }

  fun moveToTrash(id: String) {
    withResultSuspend {
      reminderRepository.getById(id)?.let {
        it.isRemoved = true
        eventControlFactory.getController(it).disable()
        reminderRepository.save(it)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          it.uuId
        )
        loadReminders()
        Commands.DELETED
      } ?: run {
        Commands.FAILED
      }
    }
  }

  private fun loadReminders() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminders = reminderRepository.getAllTypes(
        removed = false,
        active = true,
        types = TYPES
      )
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
    val groups = groupRepository.getAll()
      .map { Filter(it.groupUuId, it.groupTitle, false) }
    if (groups.isNotEmpty()) {
      filterGroups.add(
        FilterGroup(
          GROUP_FILTER_ID,
          textProvider.getString(R.string.groups),
          groups
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
      filterByQuery(reminders, lastQuery), appliedFilters?.selectedFilters?.get(GROUP_FILTER_ID)
    )
    val uiLists = uiReminderListsAdapter.convert(filtered)
    withContext(dispatcherProvider.main()) {
      _events.value = uiLists
    }
  }

  private fun filterByGroups(reminders: List<Reminder>, groupIds: List<String>?): List<Reminder> {
    if (groupIds.isNullOrEmpty()) return reminders
    return reminders.filter(ReminderGroupFilter(groupIds)).also {
      Logger.i(TAG, "Filtered by groups: ${it.size} items left, was: ${reminders.size}. Groups: ${groupIds.joinToString()}")
    }
  }

  private fun filterByQuery(reminders: List<Reminder>, query: String): List<Reminder> {
    if (query.isBlank()) return reminders
    return reminders.filter(ReminderQueryFilter(lastQuery)).also {
      Logger.i(TAG, "Filtered by query: ${it.size} items left, was: ${reminders.size}. Query: ${Logger.private(query)}")
    }
  }

  companion object {
    private const val TAG = "ActiveTodoRemindersVM"
    private val TYPES = intArrayOf(Reminder.BY_DATE_SHOP)
    private const val GROUP_FILTER_ID = "groups"
  }
}
