package com.github.naz013.group.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.logic.group.DeleteGroupUseCase
import com.github.naz013.logic.group.MakeGroupDefaultUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.group.UiGroupList
import com.github.naz013.ui.group.UiGroupListAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class GroupsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val groupV2Repository: GroupV2Repository,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val deleteGroupUseCase: DeleteGroupUseCase,
  private val makeGroupDefaultUseCase: MakeGroupDefaultUseCase,
  private val reminderV2Repository: ReminderV2Repository,
) : ViewModel() {

  private val _state = MutableStateFlow(GroupsScreenState())
  private val _selectedItemId = MutableStateFlow<String?>(null)
  val state = combine(_state, _selectedItemId, GroupsScreenState::withSelectedItem)
    .stateInWhileSubscribed(GroupsScreenState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      groupV2Repository.observeAll().collect { groups -> applyGroups(groups) }
    }
  }

  fun onSelectedItemIdChanged(id: String?) {
    _selectedItemId.value = id
  }

  fun onAddClick() {
    navigationEvent.postValue(Event(NavigationEvent.AddGroup))
  }

  fun onGroupClick(id: String) {
    navigationEvent.postValue(Event(NavigationEvent.OpenDetails(id)))
  }

  fun onGroupMenuAction(
    group: UiGroupList,
    action: GroupMenuAction,
  ) {
    when (action) {
      GroupMenuAction.EDIT -> {
        navigationEvent.postValue(Event(NavigationEvent.OpenEdit(group.id)))
      }
      GroupMenuAction.DELETE -> {
        navigationEvent.postValue(Event(NavigationEvent.ConfirmDelete(group.id)))
      }
      GroupMenuAction.MAKE_DEFAULT -> {
        viewModelScope.launch(dispatcherProvider.io()) {
          makeGroupDefaultUseCase(group.id)
        }
      }
    }
  }

  fun deleteGroup(id: String) {
    viewModelScope.launch(dispatcherProvider.io()) {
      val group = groupV2Repository.getById(id)
      if (group == null) {
        Logger.e(TAG, "Group not found: $id")
        return@launch
      }
      deleteGroupUseCase(id)
    }
  }

  // Driven by groupV2Repository.observeAll() in init - no manual reload needed after
  // deleteGroup/MAKE_DEFAULT, the Flow re-emits on its own once the use case writes through.
  private suspend fun applyGroups(groups: List<GroupV2>) {
    val uiGroups =
      withContext(dispatcherProvider.io()) {
        groups
          .map { uiGroupListAdapter.convert(it, reminderV2Repository.countActiveByGroupId(it.uuId)) }
          .sortedWith(GROUP_ORDER)
      }
    _state.update {
      it.copy(listState = if (uiGroups.isEmpty()) ListState.Empty else ListState.Ready(uiGroups))
    }
  }

  sealed interface NavigationEvent {
    data object AddGroup : NavigationEvent

    data class OpenEdit(
      val id: String,
    ) : NavigationEvent

    data class OpenDetails(
      val id: String,
    ) : NavigationEvent

    data class ConfirmDelete(
      val id: String,
    ) : NavigationEvent
  }

  companion object {
    private const val TAG = "GroupsViewModel"
    private val GROUP_ORDER =
      compareByDescending<UiGroupList> { it.isDefaultGroup }
        .thenBy { it.title }
  }
}
