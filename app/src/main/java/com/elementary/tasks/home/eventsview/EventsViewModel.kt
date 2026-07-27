package com.elementary.tasks.home.eventsview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayQueryFilter
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.reminder.lists.filter.DateRangeFilterGroup
import com.elementary.tasks.reminder.lists.filter.FilterGroup
import com.elementary.tasks.reminder.lists.filter.ReminderGroupFilter
import com.elementary.tasks.reminder.lists.filter.ReminderGroupFilterGroup
import com.elementary.tasks.reminder.lists.filter.query.ReminderV2QueryFilterInstance
import com.elementary.tasks.reminder.scheduling.usecase.SkipReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.usecase.reminders.GetRemindersV2ByRemovedStatusUseCase
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
import org.threeten.bp.LocalDate

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class EventsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val getRemindersV2ByRemovedStatusUseCase: GetRemindersV2ByRemovedStatusUseCase,
  private val groupV2Repository: GroupV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val uiEventItemAdapter: UiEventItemAdapter,
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
  private val skipReminderUseCase: SkipReminderUseCase,
  private val toggleReminderStateUseCase: ToggleReminderStateUseCase,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
) : ViewModel() {

  private val _eventsScreenState = MutableStateFlow(EventsScreenState())
  val eventsScreenState = _eventsScreenState.stateInWhileSubscribed(EventsScreenState())
    .onStart { refresh() }
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQuery = MutableStateFlow("")
  private val selectedCategories = MutableStateFlow(EventCategory.entries.toSet())
  private val refreshSignal = MutableStateFlow(0)

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      combine(
        searchQuery.debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        selectedCategories,
        refreshSignal,
      ) { query, categories, _ -> Pair(query, categories) }
        .flatMapLatest { (query, categories) ->
          flow { emit(loadMerged(query, categories)) }
        }.collect { applyList(it) }
    }
  }

  private fun refresh() {
    refreshSignal.update { it + 1 }
  }

  internal suspend fun loadMerged(
    query: String,
    categories: Set<EventCategory>,
  ): MergedResult {
    val reminderCategoriesSelected =
      categories.contains(EventCategory.REMINDERS) || categories.contains(EventCategory.SHOPPING)
    val allReminders =
      if (reminderCategoriesSelected) getRemindersV2ByRemovedStatusUseCase(removed = false) else emptyList()
    val allBirthdays = if (categories.contains(EventCategory.BIRTHDAYS)) birthdayRepository.getAll() else emptyList()
    val groups = groupV2Repository.getAll()

    val canFilter = prepareFilters(allReminders, groups, reminderCategoriesSelected)

    val filteredReminders = filterReminders(allReminders, query, categories)
    val filteredBirthdays = filterBirthdays(allBirthdays, query)

    val groupsById = groups.associateBy { it.uuId }
    val items = uiEventItemAdapter.convertV2(filteredReminders, groupsById, filteredBirthdays)
    return MergedResult(items, canFilter)
  }

  private fun applyList(result: MergedResult) {
    _eventsScreenState.update {
      it.copy(
        listState = if (result.items.isEmpty()) ListState.Empty else ListState.Ready(result.items),
      )
    }
  }

  private fun prepareFilters(
    reminders: List<ReminderV2>,
    groups: List<GroupV2>,
    reminderCategoriesSelected: Boolean,
  ): Boolean {
    val filterGroups = mutableListOf<FilterGroup>()
    val groupFilters = groups.map { ReminderGroupFilter(it.uuId, it.title) }
    if (groupFilters.isNotEmpty()) {
      filterGroups.add(
        ReminderGroupFilterGroup(
          id = GROUP_FILTER_ID,
          title = textProvider.getString(R.string.groups),
          appliedFilter = null,
          filters = groupFilters,
        ),
      )
    }

    if (reminders.isNotEmpty()) {
      var minDate = LocalDate.now()
      var maxDate = LocalDate.now()
      reminders.forEach {
        val reminderDate = it.schedule.eventDateTime?.let { dt -> dateTimeManager.utcToLocal(dt) }?.toLocalDate() ?: return@forEach
        if (reminderDate.isBefore(minDate)) {
          minDate = reminderDate
        } else if (reminderDate.isAfter(maxDate)) {
          maxDate = reminderDate
        }
      }
      filterGroups.add(
        DateRangeFilterGroup(
          id = DATE_RANGE_FILTER_ID,
          title = textProvider.getString(R.string.date_range),
          appliedFilter = null,
          minDate = minDate,
          maxDate = maxDate,
        ),
      )
    }

    return filterGroups.isNotEmpty() && reminders.isNotEmpty() && reminderCategoriesSelected
  }

  private fun filterReminders(
    reminders: List<ReminderV2>,
    query: String,
    categories: Set<EventCategory>,
  ): List<ReminderV2> {
    val byCategory =
      reminders.filter { reminder ->
        val isShopping = reminder.action is ReminderAction.Shopping
        if (isShopping) categories.contains(EventCategory.SHOPPING) else categories.contains(EventCategory.REMINDERS)
      }
    val byQuery = if (query.isBlank()) byCategory else byCategory.filter(ReminderV2QueryFilterInstance(query))
    return byQuery
  }

  private fun filterBirthdays(
    birthdays: List<Birthday>,
    query: String,
  ): List<Birthday> = if (query.isBlank()) birthdays else birthdays.filter(BirthdayQueryFilter(query))

  fun onSearchQueryChange(query: String) {
    _eventsScreenState.update { it.copy(searchQuery = query) }
    searchQuery.value = query
  }

  fun onCategoryToggle(category: EventCategory) {
    val current = selectedCategories.value
    val updated = if (current.contains(category)) current - category else current + category
    selectedCategories.value = updated
    _eventsScreenState.update { it.copy(selectedCategories = updated) }
  }

  fun onItemClick(item: UiEventItem) {
    when (item) {
      is UiEventReminder -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      is UiEventBirthday -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      is UiEventHeader -> Unit
    }
  }

  private fun onToggleReminder(item: UiEventReminder) {
    if (item.state.isGps) {
      navigationEvent.value = Event(NavigationEvent.RequestGpsPermission(item.id))
    } else {
      toggleReminder(item.id)
    }
  }

  fun toggleReminder(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val item = reminderV2Repository.getById(id) ?: return@launch
      toggleReminderStateUseCase(item)
      refresh()
    }
  }

  fun onEventMenuAction(
    item: UiEventItem,
    action: EventMenuAction,
  ) {
    when (item) {
      is UiEventReminder -> onReminderMenuAction(item, action)
      is UiEventBirthday -> onBirthdayMenuAction(item, action)
      is UiEventHeader -> Unit
    }
  }

  private fun onReminderMenuAction(
    item: UiEventReminder,
    action: EventMenuAction,
  ) {
    when (action) {
      EventMenuAction.OPEN -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      EventMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenReminderEdit(item.id))
      EventMenuAction.ARCHIVE -> navigationEvent.value = Event(NavigationEvent.ConfirmArchiveReminder(item.id))
      EventMenuAction.SKIP -> skipReminder(item.id)
      EventMenuAction.TURN_OFF -> onToggleReminder(item)
      EventMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDeleteReminder(item.id))
    }
  }

  private fun onBirthdayMenuAction(
    item: UiEventBirthday,
    action: EventMenuAction,
  ) {
    when (action) {
      EventMenuAction.OPEN -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      EventMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayEdit(item.id))
      EventMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDeleteBirthday(item.id))
      EventMenuAction.ARCHIVE, EventMenuAction.SKIP, EventMenuAction.TURN_OFF -> Unit
    }
  }

  fun skipReminder(id: String) {
    viewModelScope.launch(dispatcherProvider.io()) {
      val fromDb = reminderV2Repository.getById(id)
      if (fromDb != null) {
        skipReminderUseCase(fromDb)
        refresh()
      }
    }
  }

  fun moveReminderToArchive(id: String) {
    viewModelScope.launch(dispatcherProvider.io()) {
      moveReminderToArchiveUseCase(id)
      refresh()
    }
  }

  fun deleteReminder(id: String) {
    viewModelScope.launch(dispatcherProvider.io()) {
      val fromDb = reminderV2Repository.getById(id)
      if (fromDb != null) {
        deleteReminderUseCase(fromDb)
        refresh()
      }
    }
  }

  fun deleteBirthday(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteBirthdayUseCase(id)
      refresh()
    }
  }

  fun onAddReminderClick() {
    navigationEvent.value = Event(NavigationEvent.OpenNewReminder)
  }

  fun onAddShoppingClick() {
    navigationEvent.value = Event(NavigationEvent.OpenNewShoppingReminder)
  }

  fun onAddBirthdayClick() {
    navigationEvent.value = Event(NavigationEvent.OpenNewBirthday)
  }

  fun onArchiveClick() {
    navigationEvent.value = Event(NavigationEvent.OpenArchive)
  }

  fun onGroupsClick() {
    navigationEvent.value = Event(NavigationEvent.OpenGroups)
  }

  data class MergedResult(
    val items: List<UiEventItem>,
    val canFilter: Boolean,
  )

  sealed interface NavigationEvent {
    data class OpenReminderPreview(
      val id: String,
    ) : NavigationEvent

    data class OpenReminderEdit(
      val id: String,
    ) : NavigationEvent

    data object OpenNewReminder : NavigationEvent

    data object OpenNewShoppingReminder : NavigationEvent

    data class OpenBirthdayPreview(
      val id: String,
    ) : NavigationEvent

    data class OpenBirthdayEdit(
      val id: String,
    ) : NavigationEvent

    data object OpenNewBirthday : NavigationEvent

    data object OpenArchive : NavigationEvent

    data object OpenGroups : NavigationEvent

    data class RequestGpsPermission(
      val id: String,
    ) : NavigationEvent

    data class ConfirmArchiveReminder(
      val id: String,
    ) : NavigationEvent

    data class ConfirmDeleteReminder(
      val id: String,
    ) : NavigationEvent

    data class ConfirmDeleteBirthday(
      val id: String,
    ) : NavigationEvent
  }

  companion object {
    private const val TAG = "EventsViewModel"
    private const val SEARCH_DEBOUNCE_MS = 300L
    private const val GROUP_FILTER_ID = "groups"
    private const val DATE_RANGE_FILTER_ID = "date_range"
  }
}
