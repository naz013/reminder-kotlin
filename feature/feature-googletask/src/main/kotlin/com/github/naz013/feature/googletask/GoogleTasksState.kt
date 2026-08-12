package com.github.naz013.feature.googletask

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.ui.tag.TagChipState

internal data class GoogleTasksState(
  val isLoggedIn: Boolean = false,
  val isLoading: Boolean = false,
  val taskLists: List<UiGoogleTaskListEntry> = emptyList(),
  val tasks: List<GoogleTaskItemState> = emptyList(),
  val fabContainerColor: Color? = null,
  val fabContentColor: Color? = null,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagId: String? = null,
)

internal data class UiGoogleTaskListEntry(
  val id: String,
  val title: String,
  @ColorInt val color: Int, // TODO: Change to Compose color
)
