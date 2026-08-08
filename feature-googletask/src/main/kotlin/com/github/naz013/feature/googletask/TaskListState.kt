package com.github.naz013.feature.googletask

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.googletask.GoogleTaskItemState

internal data class TaskListState(
  val listId: String = "",
  val title: String = "",
  val isLoading: Boolean = false,
  val isSyncing: Boolean = false,
  val isDefaultList: Boolean = false,
  val tasks: List<GoogleTaskItemState> = emptyList(),
  val fabContainerColor: Color? = null,
  val fabContentColor: Color? = null,
  val canDelete: Boolean = false,
  val showDeleteConfirm: Boolean = false,
)
