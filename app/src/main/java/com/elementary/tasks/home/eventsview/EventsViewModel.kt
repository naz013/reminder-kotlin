package com.elementary.tasks.home.eventsview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.BirthdayQueryFilter
import com.elementary.tasks.birthdays.BirthdaySmartListPredicate
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.reminder.lists.filter.query.ReminderV2QueryFilterInstance
import com.elementary.tasks.reminder.scheduling.usecase.SkipReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
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
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.usecase.reminders.GetRemindersV2ByRemovedStatusUseCase
import com.github.naz013.usecase.reminders.smartlist.ReminderSmartListPredicate
import com.github.naz013.usecase.reminders.smartlist.SmartListFilter
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

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class EventsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val getRemindersV2ByRemovedStatusUseCase: GetRemindersV2ByRemovedStatusUseCase,
  private val groupV2Repository: GroupV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val uiEventItemAdapter: UiEventItemAdapter,
  private val dateTimeManager: DateTimeManager,
  private val birthdaySmartListPredicate: BirthdaySmartListPredicate,
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
  private val selectedSmartList = MutableStateFlow<SmartListFilter?>(null)
  private val selectedTagId = MutableStateFlow<String?>(null)
  private val selectedGroupId = MutableStateFlow<String?>(null)
  private val refreshSignal = MutableStateFlow(0)

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      val filterCriteria =
        combine(
          searchQuery.debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
          selectedCategories,
          selectedSmartList,
          selectedTagId,
          selectedGroupId,
        ) { query, categories, smartList, tagId, groupId ->
          FilterCriteria(query, categories, smartList, tagId, groupId)
        }
      combine(filterCriteria, refreshSignal) { criteria, _ -> criteria }
        .flatMapLatest { criteria ->
          flow {
            emit(
              loadMerged(
                query = criteria.query,
                categories = criteria.categories,
                smartList = criteria.smartList,
                tagId = criteria.tagId,
                groupId = criteria.groupId,
              ),
            )
          }
        }.collect { applyList(it) }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      refreshSignal.collect { refreshHasAnyItems() }
    }
  }

  private fun refresh() {
    refreshSignal.update { it + 1 }
  }

  private suspend fun refreshHasAnyItems() {
    val hasAnyItems =
      getRemindersV2ByRemovedStatusUseCase(removed = false).isNotEmpty() || birthdayRepository.getAll().isNotEmpty()
    _eventsScreenState.update { it.copy(hasAnyItems = hasAnyItems) }
  }

  internal suspend fun loadMerged(
    query: String,
    categories: Set<EventCategory>,
    smartList: SmartListFilter? = null,
    tagId: String? = null,
    groupId: String? = null,
  ): MergedResult {
    val reminderCategoriesSelected =
      categories.contains(EventCategory.REMINDERS) || categories.contains(EventCategory.SHOPPING)
    val allReminders =
      if (reminderCategoriesSelected) getRemindersV2ByRemovedStatusUseCase(removed = false) else emptyList()
    val allBirthdays = if (categories.contains(EventCategory.BIRTHDAYS)) birthdayRepository.getAll() else emptyList()
    val groups = groupV2Repository.getAll()
    val tags = tagRepository.getAll()

    val filteredReminders = filterReminders(allReminders, query, categories, smartList, tagId, groupId)
    val filteredBirthdays = filterBirthdays(allBirthdays, query, smartList, tagId, groupId)

    val groupsById = groups.associateBy { it.uuId }
    val items = uiEventItemAdapter.convertV2(filteredReminders, groupsById, filteredBirthdays)
    return MergedResult(items, tags, groups)
  }

  private fun applyList(result: MergedResult) {
    _eventsScreenState.update {
      it.copy(
        listState = if (result.items.isEmpty()) ListState.Empty else ListState.Ready(result.items),
        availableTags = result.availableTags,
        availableGroups = result.availableGroups,
      )
    }
  }

  private suspend fun filterReminders(
    reminders: List<ReminderV2>,
    query: String,
    categories: Set<EventCategory>,
    smartList: SmartListFilter?,
    tagId: String?,
    groupId: String?,
  ): List<ReminderV2> {
    val byCategory =
      reminders.filter { reminder ->
        val isShopping = reminder.action is ReminderAction.Shopping
        if (isShopping) categories.contains(EventCategory.SHOPPING) else categories.contains(EventCategory.REMINDERS)
      }
    val byQuery = if (query.isBlank()) byCategory else byCategory.filter(ReminderV2QueryFilterInstance(query))
    val bySmartList =
      if (smartList == null) {
        byQuery
      } else {
        // reminder.schedule.eventDateTime is stored in UTC (see UiReminderListAdapter's
        // utcToLocal conversion for the same field) - comparing it against local "now" would
        // silently misclassify Today/Overdue/This week in any non-UTC timezone.
        val now = dateTimeManager.localToUtc(dateTimeManager.getCurrentDateTime())
        byQuery.filter { ReminderSmartListPredicate.matches(smartList, it, now) }
      }
    val byGroup = if (groupId == null) bySmartList else bySmartList.filter { it.groupId == groupId }
    val byTag =
      if (tagId == null) {
        byGroup
      } else {
        val taggedIds = tagAssignmentRepository.getItemIdsForTag(tagId, TaggedItemType.REMINDER).toSet()
        byGroup.filter { it.uuId in taggedIds }
      }
    return byTag
  }

  private fun filterBirthdays(
    birthdays: List<Birthday>,
    query: String,
    smartList: SmartListFilter?,
    tagId: String?,
    groupId: String?,
  ): List<Birthday> {
    // Birthdays have no tag or group concept, so an active Tag/Group filter is an explicit
    // request to narrow down to labeled items - birthdays vacuously don't match and drop out.
    if (tagId != null || groupId != null) return emptyList()

    val byQuery = if (query.isBlank()) birthdays else birthdays.filter(BirthdayQueryFilter(query))
    if (smartList == null) return byQuery

    val today = dateTimeManager.getCurrentDateTime().toLocalDate()
    return byQuery.filter { birthdaySmartListPredicate.matches(smartList, it, today) }
  }

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

  fun onSmartListSelected(filter: SmartListFilter?) {
    val updated = if (selectedSmartList.value == filter) null else filter
    selectedSmartList.value = updated
    _eventsScreenState.update { it.copy(selectedSmartList = updated) }
  }

  fun onTagFilterSelected(tagId: String?) {
    val updated = if (selectedTagId.value == tagId) null else tagId
    selectedTagId.value = updated
    _eventsScreenState.update { it.copy(selectedTagId = updated) }
  }

  fun onGroupFilterSelected(groupId: String?) {
    val updated = if (selectedGroupId.value == groupId) null else groupId
    selectedGroupId.value = updated
    _eventsScreenState.update { it.copy(selectedGroupId = updated) }
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

  fun onTagsClick() {
    navigationEvent.value = Event(NavigationEvent.OpenTags)
  }

  data class MergedResult(
    val items: List<UiEventItem>,
    val availableTags: List<Tag> = emptyList(),
    val availableGroups: List<GroupV2> = emptyList(),
  )

  private data class FilterCriteria(
    val query: String,
    val categories: Set<EventCategory>,
    val smartList: SmartListFilter?,
    val tagId: String?,
    val groupId: String?,
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

    data object OpenTags : NavigationEvent

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
  }
}
