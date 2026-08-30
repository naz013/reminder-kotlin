package com.github.naz013.group.list

import com.github.naz013.ui.group.UiGroupList

internal data class GroupsScreenState(
  val listState: ListState = ListState.Loading,
)

internal fun GroupsScreenState.withSelectedItem(selectedItemId: String?): GroupsScreenState {
  val ready = listState as? ListState.Ready ?: return this
  return copy(
    listState = ListState.Ready(ready.groups.map { it.copy(isSelected = it.id == selectedItemId) }),
  )
}

internal sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val groups: List<UiGroupList>,
  ) : ListState

  data object Empty : ListState
}

internal enum class GroupMenuAction { EDIT, DELETE, MAKE_DEFAULT }
