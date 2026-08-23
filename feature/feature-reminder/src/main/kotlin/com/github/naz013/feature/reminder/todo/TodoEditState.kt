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
  val hapticFeedbackEnabled: Boolean = true,
  /** Only true while creating a brand-new todo and at least one cloud storage is logged in -
   *  can't be turned on once it's been saved, and pointless to offer with nothing to opt out of. */
  val canSetOfflineOnly: Boolean = false,
  val offlineOnlyChecked: Boolean = false,
)
