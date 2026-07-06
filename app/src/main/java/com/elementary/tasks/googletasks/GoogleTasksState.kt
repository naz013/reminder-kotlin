package com.elementary.tasks.googletasks

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList

data class GoogleTasksState(
  val isLoggedIn: Boolean = false,
  val isLoading: Boolean = false,
  val taskLists: List<UiGoogleTaskListEntry> = emptyList(),
  val tasks: List<UiGoogleTaskList> = emptyList(),
  val fabContainerColor: Color? = null,
  val fabContentColor: Color? = null,
)

data class UiGoogleTaskListEntry(
  val id: String,
  val title: String,
  @ColorInt val color: Int, // TODO: Change to Compose color
)
