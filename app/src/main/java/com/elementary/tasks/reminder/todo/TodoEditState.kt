package com.elementary.tasks.reminder.todo

import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.github.naz013.ui.tag.TagChipState

data class TodoEditState(
  val title: String = "",
  val subTasksItem: SubTasksBuilderItem? = null,
  val groupItem: GroupBuilderItem? = null,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
  val canSave: Boolean = false,
)
