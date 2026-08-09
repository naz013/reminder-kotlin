package com.elementary.tasks.reminder.build

import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPrediction
import com.github.naz013.ui.tag.TagChipState

data class BuildReminderState(
  val builderItems: List<UiBuilderItem> = emptyList(),
  val prediction: ReminderPrediction? = null,
  val canSave: Boolean = false,
  val canRemove: Boolean = false,
  val isRemoved: Boolean = false,
  val canSaveAsPreset: Boolean = false,
  val saveAsPresetChecked: Boolean = false,
  val presetName: String = "",
  val editingItem: Pair<Int, BuilderItem<*>>? = null,
  val isFromFile: Boolean = false,
  val hasSameInDb: Boolean = false,
  val is24HourFormat: Boolean = true,
  val hapticFeedbackEnabled: Boolean = true,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
)
