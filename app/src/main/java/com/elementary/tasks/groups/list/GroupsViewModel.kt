package com.elementary.tasks.groups.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.groups.usecase.DeleteReminderGroupUseCase
import com.elementary.tasks.groups.usecase.MakeGroupDefaultUseCase
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val deleteReminderGroupUseCase: DeleteReminderGroupUseCase,
  private val makeGroupDefaultUseCase: MakeGroupDefaultUseCase,
) : ViewModel() {

  private val _state = MutableStateFlow(GroupsScreenState())
  val state = _state.stateInWhileSubscribed(GroupsScreenState())
    .onStart { loadGroups() }
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  fun onAddClick() {
    navigationEvent.postValue(Event(NavigationEvent.AddGroup))
  }

  fun onGroupClick(id: String) {
    navigationEvent.postValue(Event(NavigationEvent.OpenEdit(id)))
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
      val reminderGroup = reminderGroupRepository.getById(id)
      if (reminderGroup == null) {
        Logger.e(TAG, "Group not found: $id")
        return@launch
      }
      deleteReminderGroupUseCase(id)
      loadGroups()
    }
  }

  private fun loadGroups() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val groups =
        reminderGroupRepository
          .getAll()
          .map {
            uiGroupListAdapter.convert(it)
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
