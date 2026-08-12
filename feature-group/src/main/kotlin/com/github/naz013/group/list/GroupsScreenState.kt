package com.github.naz013.group.list

import com.github.naz013.ui.group.UiGroupList

internal data class GroupsScreenState(
  val listState: ListState = ListState.Loading,
)

internal sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val groups: List<UiGroupList>,
  ) : ListState

  data object Empty : ListState
}

internal enum class GroupMenuAction { EDIT, DELETE, MAKE_DEFAULT }
