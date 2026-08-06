package com.elementary.tasks.reminder.lists.removed

import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.github.naz013.domain.reminder.v2.ReminderV2

data class RemindersArchiveScreenState(
  val listState: ListState = ListState.Loading,
  val searchQuery: String = "",
  val allReminders: List<ReminderV2> = emptyList(),
  val filteredReminders: List<ReminderV2> = emptyList(),
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
