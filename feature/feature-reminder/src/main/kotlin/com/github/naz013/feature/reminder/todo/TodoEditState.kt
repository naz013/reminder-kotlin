package com.github.naz013.feature.reminder.todo

import com.github.naz013.ui.group.UiGroupList
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.ui.tag.TagChipState

internal data class TodoEditState(
  val title: String = "",
  val subTasksItem: SubTasksBuilderItem? = null,
  val availableGroups: List<UiGroupList> = emptyList(),
  val selectedGroup: UiGroupList? = null,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
  val canSave: Boolean = false,
  val isEditing: Boolean = false,
  val isRemoved: Boolean = false,
)
