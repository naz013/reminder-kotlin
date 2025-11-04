package com.elementary.tasks.reminder.lists.active

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.elementary.tasks.reminder.lists.data.UiReminderListsAdapter
import com.elementary.tasks.reminder.lists.filter.AppliedFilters
import com.elementary.tasks.reminder.lists.filter.DateRangeAppliedFilter
import com.elementary.tasks.reminder.lists.filter.DateRangeFilterGroup
import com.elementary.tasks.reminder.lists.filter.FilterGroup
import com.elementary.tasks.reminder.lists.filter.Filters
import com.elementary.tasks.reminder.lists.filter.ReminderGroupAppliedFilter
import com.elementary.tasks.reminder.lists.filter.ReminderGroupFilter
import com.elementary.tasks.reminder.lists.filter.ReminderGroupFilterGroup
import com.elementary.tasks.reminder.lists.filter.daterange.ReminderDateRangeFilterInstance
import com.elementary.tasks.reminder.lists.filter.group.ReminderGroupFilterInstance
import com.elementary.tasks.reminder.lists.filter.query.ReminderQueryFilterInstance
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.elementary.tasks.reminder.usecase.ScheduleReminderUploadUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
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
import org.threeten.bp.LocalDate

@OptIn(FlowPreview::class)
class ActiveRemindersViewModel(
  dispatcherProvider: DispatcherProvider,
  private val reminderRepository: ReminderRepository,
  private val eventControlFactory: EventControlFactory,
  private val uiReminderListsAdapter: UiReminderListsAdapter,
  private val textProvider: TextProvider,
  private val groupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
  private val scheduleReminderUploadUseCase: ScheduleReminderUploadUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  private val _events = mutableLiveDataOf<List<UiReminderEventsList>>()
  val events = _events.toLiveData()

  private val _showFilters = mutableLiveEventOf<Filters>()
  val showFilters = _showFilters.toLiveData()

  private val _canFilter = mutableLiveDataOf<Boolean>()
  val canFilter = _canFilter.toLiveData()

  private val _canSearch = mutableLiveDataOf<Boolean>()
  val canSearch = _canSearch.toLiveData()

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
    Logger.i(TAG, "Show filters requested")
    if (allFilters.isEmpty()) {
      Logger.e(TAG, "No filters to show")
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

          is DateRangeFilterGroup -> {
            DateRangeFilterGroup(
              id = group.id,
              title = group.title,
              appliedFilter = appliedFilter as? DateRangeAppliedFilter,
              minDate = group.minDate,
              maxDate = group.maxDate
            )
          }

          else -> group
        }
      }
      withContext(dispatcherProvider.main()) {
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

  fun skip(id: String) {
    withResultSuspend {
      val fromDb = reminderRepository.getById(id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        scheduleReminderUploadUseCase(fromDb.uuId)
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
        scheduleReminderUploadUseCase(item.uuId)
        postInProgress(false)
        postCommand(Commands.SAVED)
      }
      loadReminders()
    }
  }

  fun moveToTrash(id: String) {
    withResultSuspend {
      moveReminderToArchiveUseCase(id)
      loadReminders()
      Commands.DELETED
    }
  }

  private fun loadReminders() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminders = reminderRepository.getByRemovedStatus(removed = false)
      prepareFilters()
      filterReminders()
      postInProgress(false)
      Logger.i(
        TAG,
        "Loaded ${reminders.size} active reminders. can search: ${reminders.isNotEmpty()}"
      )
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

    var minDate = LocalDate.now()
    var maxDate = LocalDate.now()
    reminders.forEach {
      val reminderDate = dateTimeManager.fromGmtToLocal(it.eventTime)?.toLocalDate() ?: return@forEach
      if (reminderDate.isBefore(minDate)) {
        minDate = reminderDate
      } else if (reminderDate.isAfter(maxDate)) {
        maxDate = reminderDate
      }
    }
    if (minDate != maxDate || reminders.isNotEmpty()) {
      filterGroups.add(
        DateRangeFilterGroup(
          id = DATE_RANGE_FILTER_ID,
          title = textProvider.getString(R.string.date_range),
          appliedFilter = null,
          minDate = minDate,
          maxDate = maxDate
        )
      )
    }

    allFilters = filterGroups
    withContext(dispatcherProvider.main()) {
      _canFilter.value = filterGroups.isNotEmpty() && reminders.isNotEmpty()
    }
  }

  private suspend fun filterReminders() {
    val filtered = filterByDateRange(
      reminders = filterByGroups(
        reminders = filterByQuery(
          reminders = reminders,
          query = lastQuery
        ),
        groupIds = (appliedFilters.selectedFilters[GROUP_FILTER_ID] as? ReminderGroupAppliedFilter)?.selectedFilterIds
      ),
      filter = appliedFilters.selectedFilters.values.filterIsInstance<DateRangeAppliedFilter>()
        .firstOrNull()
    )
    val uiLists = uiReminderListsAdapter.convert(filtered)
    withContext(dispatcherProvider.main()) {
      _events.value = uiLists
    }
  }

  private fun filterByDateRange(
    reminders: List<Reminder>,
    filter: DateRangeAppliedFilter?
  ): List<Reminder> {
    if (filter == null) return reminders
    return reminders.filter(
      ReminderDateRangeFilterInstance(
        dateTimeManager = dateTimeManager,
        startDate = filter.startDate,
        endDate = filter.endDate
      )
    ).also {
      Logger.i(
        TAG,
        "Filtered by date range: ${it.size} items left, was: ${reminders.size}. Range: ${filter.startDate} - ${filter.endDate}"
      )
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
    private const val TAG = "ActiveRemindersViewModel"
    private const val GROUP_FILTER_ID = "groups"
    private const val DATE_RANGE_FILTER_ID = "date_range"
  }
}
