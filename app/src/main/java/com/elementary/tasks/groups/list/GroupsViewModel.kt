package com.elementary.tasks.groups.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.groups.usecase.DeleteGroupUseCase
import com.elementary.tasks.groups.usecase.MakeGroupDefaultUseCase
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.usecase.reminders.CountActiveRemindersV2ByGroupIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val groupV2Repository: GroupV2Repository,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val deleteGroupUseCase: DeleteGroupUseCase,
  private val makeGroupDefaultUseCase: MakeGroupDefaultUseCase,
  private val countActiveRemindersV2ByGroupIdUseCase: CountActiveRemindersV2ByGroupIdUseCase,
) : ViewModel() {

  private val _state = MutableStateFlow(GroupsScreenState())
  val state = _state.stateInWhileSubscribed(GroupsScreenState())
    .onStart { loadGroups() }
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  fun refreshState() {
    loadGroups()
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
          loadGroups()
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
      loadGroups()
    }
  }

  private fun loadGroups() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val groups =
        groupV2Repository
          .getAll()
          .map {
            uiGroupListAdapter.convert(it, countActiveRemindersV2ByGroupIdUseCase(it.uuId))
          }.sortedWith(GROUP_ORDER)

      withContext(dispatcherProvider.main()) {
        _state.update {
          it.copy(listState = if (groups.isEmpty()) ListState.Empty else ListState.Ready(groups))
        }
      }
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
