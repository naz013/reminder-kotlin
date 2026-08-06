package com.elementary.tasks.groups.list

import com.elementary.tasks.core.data.ui.group.UiGroupList

data class GroupsScreenState(
  val listState: ListState = ListState.Loading,
)

sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val groups: List<UiGroupList>,
  ) : ListState

  data object Empty : ListState
}

enum class GroupMenuAction { EDIT, DELETE, MAKE_DEFAULT }
