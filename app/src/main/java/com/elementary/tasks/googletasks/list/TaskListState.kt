package com.elementary.tasks.googletasks.list

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList

data class TaskListState(
  val title: String = "",
  val isLoading: Boolean = false,
  val tasks: List<UiGoogleTaskList> = emptyList(),
  val fabContainerColor: Color? = null,
  val fabContentColor: Color? = null,
  val canDelete: Boolean = false,
)

sealed interface TaskListEvent {
  data object Deleted : TaskListEvent
}
