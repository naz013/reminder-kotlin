package com.elementary.tasks.home.agenda

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.logic.birthday.BirthdayQueryFilter
import com.github.naz013.logic.birthday.BirthdaySmartListPredicate
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.feature.reminder.lists.filter.query.ReminderV2QueryFilterInstance
import com.github.naz013.logic.reminder.usecase.SkipReminderUseCase
import com.github.naz013.logic.reminder.usecase.ToggleReminderStateUseCase
import com.github.naz013.feature.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.datecalc.DateTimeManager
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
import com.github.naz013.logic.reminder.smartlist.ReminderSmartListPredicate
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
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
class AgendaViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val uiAgendaItemAdapter: UiAgendaItemAdapter,
  private val dateTimeManager: DateTimeManager,
  private val birthdaySmartListPredicate: BirthdaySmartListPredicate,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
  private val skipReminderUseCase: SkipReminderUseCase,
  private val toggleReminderStateUseCase: ToggleReminderStateUseCase,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
) : ViewModel() {

  private val _agendaScreenState = MutableStateFlow(AgendaScreenState())
  val agendaScreenState = _agendaScreenState.stateInWhileSubscribed(AgendaScreenState())
    .onStart { refresh() }
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private val searchQuery = MutableStateFlow("")
  private val selectedCategories = MutableStateFlow(AgendaCategory.entries.toSet())
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
      reminderV2Repository.getByRemovedStatus(removed = false).isNotEmpty() || birthdayRepository.getAll().isNotEmpty()
    _agendaScreenState.update { it.copy(hasAnyItems = hasAnyItems) }
  }

  internal suspend fun loadMerged(
    query: String,
    categories: Set<AgendaCategory>,
    smartList: SmartListFilter? = null,
    tagId: String? = null,
    groupId: String? = null,
  ): MergedResult {
    val reminderCategoriesSelected =
      categories.contains(AgendaCategory.REMINDERS) ||
        categories.contains(AgendaCategory.SHOPPING) ||
        categories.contains(AgendaCategory.LOCATION)
    val allReminders =
      if (reminderCategoriesSelected) reminderV2Repository.getByRemovedStatus(removed = false) else emptyList()
    val allBirthdays = if (categories.contains(AgendaCategory.BIRTHDAYS)) birthdayRepository.getAll() else emptyList()
    val groups = groupV2Repository.getAll()
    val tags = tagRepository.getAll()

    val filteredReminders = filterReminders(allReminders, query, categories, smartList, tagId, groupId)
    val filteredBirthdays = filterBirthdays(allBirthdays, query, smartList, tagId, groupId)

    val groupsById = groups.associateBy { it.uuId }
    val items = uiAgendaItemAdapter.convertV2(filteredReminders, groupsById, filteredBirthdays)
    return MergedResult(items, tags, groups)
  }

  private fun applyList(result: MergedResult) {
    _agendaScreenState.update {
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
    categories: Set<AgendaCategory>,
    smartList: SmartListFilter?,
    tagId: String?,
    groupId: String?,
  ): List<ReminderV2> {
    val byCategory =
      reminders.filter { reminder ->
        when {
          reminder.action is ReminderAction.Shopping -> categories.contains(AgendaCategory.SHOPPING)
          reminder.location != null -> categories.contains(AgendaCategory.LOCATION)
          else -> categories.contains(AgendaCategory.REMINDERS)
        }
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

  private suspend fun filterBirthdays(
    birthdays: List<Birthday>,
    query: String,
    smartList: SmartListFilter?,
    tagId: String?,
    groupId: String?,
  ): List<Birthday> {
    // Birthdays have no group concept, so an active Group filter is an explicit request to
    // narrow down to grouped items - birthdays vacuously don't match and drop out.
    if (groupId != null) return emptyList()

    val byQuery = if (query.isBlank()) birthdays else birthdays.filter(BirthdayQueryFilter(query))
    val bySmartList =
      if (smartList == null) {
        byQuery
      } else {
        val today = dateTimeManager.getCurrentDateTime().toLocalDate()
        byQuery.filter { birthdaySmartListPredicate.matches(smartList, it, today) }
      }
    return if (tagId == null) {
      bySmartList
    } else {
      val taggedIds = tagAssignmentRepository.getItemIdsForTag(tagId, TaggedItemType.BIRTHDAY).toSet()
      bySmartList.filter { it.uuId in taggedIds }
    }
  }

  fun onSearchQueryChange(query: String) {
    _agendaScreenState.update { it.copy(searchQuery = query) }
    searchQuery.value = query
  }

  fun onCategoryToggle(category: AgendaCategory) {
    val current = selectedCategories.value
    val updated = if (current.contains(category)) current - category else current + category
    selectedCategories.value = updated
    _agendaScreenState.update { it.copy(selectedCategories = updated) }
  }

  fun onSmartListSelected(filter: SmartListFilter?) {
    val updated = if (selectedSmartList.value == filter) null else filter
    selectedSmartList.value = updated
    _agendaScreenState.update { it.copy(selectedSmartList = updated) }
  }

  fun onTagFilterSelected(tagId: String?) {
    val updated = if (selectedTagId.value == tagId) null else tagId
    selectedTagId.value = updated
    _agendaScreenState.update { it.copy(selectedTagId = updated) }
  }

  fun onGroupFilterSelected(groupId: String?) {
    val updated = if (selectedGroupId.value == groupId) null else groupId
    selectedGroupId.value = updated
    _agendaScreenState.update { it.copy(selectedGroupId = updated) }
  }

  fun onItemClick(item: UiAgendaItem) {
    when (item) {
      is UiAgendaReminder -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      is UiAgendaBirthday -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      is UiAgendaHeader -> Unit
    }
  }

  private fun onToggleReminder(item: UiAgendaReminder) {
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

  fun onAgendaMenuAction(
    item: UiAgendaItem,
    action: AgendaMenuAction,
  ) {
    when (item) {
      is UiAgendaReminder -> onReminderMenuAction(item, action)
      is UiAgendaBirthday -> onBirthdayMenuAction(item, action)
      is UiAgendaHeader -> Unit
    }
  }

  private fun onReminderMenuAction(
    item: UiAgendaReminder,
    action: AgendaMenuAction,
  ) {
    when (action) {
      AgendaMenuAction.OPEN -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      AgendaMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenReminderEdit(item.id))
      AgendaMenuAction.ARCHIVE -> navigationEvent.value = Event(NavigationEvent.ConfirmArchiveReminder(item.id))
      AgendaMenuAction.SKIP -> skipReminder(item.id)
      AgendaMenuAction.TURN_OFF -> onToggleReminder(item)
      AgendaMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDeleteReminder(item.id))
    }
  }

  private fun onBirthdayMenuAction(
    item: UiAgendaBirthday,
    action: AgendaMenuAction,
  ) {
    when (action) {
      AgendaMenuAction.OPEN -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      AgendaMenuAction.EDIT -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayEdit(item.id))
      AgendaMenuAction.DELETE -> navigationEvent.value = Event(NavigationEvent.ConfirmDeleteBirthday(item.id))
      AgendaMenuAction.ARCHIVE, AgendaMenuAction.SKIP, AgendaMenuAction.TURN_OFF -> Unit
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

  fun onAddTodoClick() {
    navigationEvent.value = Event(NavigationEvent.OpenNewTodo)
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
    val items: List<UiAgendaItem>,
    val availableTags: List<Tag> = emptyList(),
    val availableGroups: List<GroupV2> = emptyList(),
  )

  private data class FilterCriteria(
    val query: String,
    val categories: Set<AgendaCategory>,
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

    data object OpenNewTodo : NavigationEvent

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
    private const val TAG = "AgendaViewModel"
    private const val SEARCH_DEBOUNCE_MS = 300L
  }
}
