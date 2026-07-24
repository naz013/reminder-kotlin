package com.elementary.tasks.googletasks.list

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList

data class TaskListState(
  val listId: String = "",
  val title: String = "",
  val isLoading: Boolean = false,
  val isSyncing: Boolean = false,
  val isDefaultList: Boolean = false,
  val tasks: List<UiGoogleTaskList> = emptyList(),
  val fabContainerColor: Color? = null,
  val fabContentColor: Color? = null,
  val canDelete: Boolean = false,
  val showDeleteConfirm: Boolean = false,
)
