package com.elementary.tasks.reminder.lists.removed

import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.github.naz013.domain.Reminder

data class RemindersArchiveScreenState(
  val listState: ListState = ListState.Loading,
  val searchQuery: String = "",
  val allReminders: List<Reminder> = emptyList(),
  val filteredReminders: List<Reminder> = emptyList(),
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
