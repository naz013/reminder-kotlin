package com.elementary.tasks.googletasks.tasklist

import androidx.compose.ui.graphics.Color

data class EditGoogleTaskListState(
  val name: String = "",
  val nameError: Boolean = false,
  val sliderColors: List<Color> = emptyList(),
  val colorIndex: Int = 0,
  val isDefault: Boolean = false,
  val isDefaultLocked: Boolean = false,
  val isLoading: Boolean = false,
  val canDelete: Boolean = false,
  val hasId: Boolean = false,
  val showDeleteConfirm: Boolean = false,
)

sealed interface EditGoogleTaskListEvent {
  data object Saved : EditGoogleTaskListEvent

  data object Deleted : EditGoogleTaskListEvent
}
