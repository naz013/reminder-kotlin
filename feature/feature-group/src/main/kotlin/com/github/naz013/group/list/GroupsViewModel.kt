package com.github.naz013.group.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.logic.group.DeleteGroupUseCase
import com.github.naz013.logic.group.MakeGroupDefaultUseCase
import com.github.naz013.logic.group.SaveGroupUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.selection.clearSelection
import com.github.naz013.ui.common.selection.select
import com.github.naz013.ui.common.selection.selectedCount
import com.github.naz013.ui.common.selection.selectedIds
import com.github.naz013.ui.common.selection.toggleSelection
import com.github.naz013.ui.group.UiGroupList
import com.github.naz013.ui.group.UiGroupListAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class GroupsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val textProvider: TextProvider,
  private val groupV2Repository: GroupV2Repository,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val deleteGroupUseCase: DeleteGroupUseCase,
  private val makeGroupDefaultUseCase: MakeGroupDefaultUseCase,
  private val saveGroupUseCase: SaveGroupUseCase,
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
    if (_state.value.selectedCount > 0) {
      updateSelection { it.toggleSelection(id) }
    } else {
      navigationEvent.postValue(Event(NavigationEvent.OpenDetails(id)))
    }
  }

  fun onGroupLongClick(id: String) {
    updateSelection { it.select(id) }
  }

  fun onSelectionCancel() {
    updateSelection { it.clearSelection() }
  }

  private fun updateSelection(transform: (List<UiGroupList>) -> List<UiGroupList>) {
    _state.update { state ->
      val listState = state.listState
      if (listState !is ListState.Ready) return@update state
      val groups = transform(listState.groups)
      state.copy(listState = ListState.Ready(groups), selectedCount = groups.selectedCount())
    }
  }

  private fun selectedGroups(): List<UiGroupList> =
    (_state.value.listState as? ListState.Ready)?.groups.orEmpty().filter { it.isSelected }

  private fun selectedIds(): Set<String> =
    (_state.value.listState as? ListState.Ready)?.groups.orEmpty().selectedIds()

  fun onDeleteSelectedClick() {
    val selected = selectedGroups()
    if (selected.isEmpty() || selected.any { it.isDefaultGroup }) return
    navigationEvent.postValue(
      Event(
        NavigationEvent.ConfirmDeleteSelected(
          ids = selected.map { it.id }.toSet(),
          title = textProvider.getText(R.string.groups_delete_selected_permanently, selected.size),
        ),
      ),
    )
  }

  fun deleteSelectedGroups(ids: Set<String>) {
    viewModelScope.launch(dispatcherProvider.io()) {
      ids.forEach { deleteGroupUseCase(it) }

      withContext(dispatcherProvider.main()) {
        onSelectionCancel()
      }
    }
  }

  fun applySelectedColor(colorPosition: Int) {
    val ids = selectedIds()
    if (ids.isEmpty()) return
    viewModelScope.launch(dispatcherProvider.io()) {
      ids.forEach { id ->
        val group = groupV2Repository.getById(id) ?: return@forEach
        saveGroupUseCase(group.copy(color = colorPosition))
      }

      withContext(dispatcherProvider.main()) {
        onSelectionCancel()
      }
    }
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

    data class ConfirmDeleteSelected(
      val ids: Set<String>,
      val title: String,
    ) : NavigationEvent
  }

  companion object {
    private const val TAG = "GroupsViewModel"
    private val GROUP_ORDER =
      compareByDescending<UiGroupList> { it.isDefaultGroup }
        .thenBy { it.title }
  }
}
