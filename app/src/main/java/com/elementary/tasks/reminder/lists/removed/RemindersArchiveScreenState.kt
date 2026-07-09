package com.elementary.tasks.reminder.lists.removed

import com.elementary.tasks.reminder.lists.data.UiReminderList

data class RemindersArchiveScreenState(
  val listState: ListState = ListState.Loading,
  val searchQuery: String = "",
)

sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val items: List<UiReminderList>,
  ) : ListState

  data object Empty : ListState
}

enum class ArchiveReminderMenuAction {
  EDIT,
  DELETE,
}
