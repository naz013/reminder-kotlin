package com.github.naz013.feature.reminder.build

import com.github.naz013.feature.reminder.build.logic.builderstate.ReminderPrediction
import com.github.naz013.ui.tag.TagChipState

internal data class BuildReminderState(
  val isLoadingForEdit: Boolean = false,
  val builderItems: List<UiBuilderItem> = emptyList(),
  val prediction: ReminderPrediction? = null,
  val canSave: Boolean = false,
  val canRemove: Boolean = false,
  val isRemoved: Boolean = false,
  val canSaveAsPreset: Boolean = false,
  val saveAsPresetChecked: Boolean = false,
  val presetName: String = "",
  /** Only shown while creating a brand-new reminder ([BuildReminderViewModel.originalV2] null) -
   * the flag can't be turned on for an already-persisted reminder. */
  val canSetOfflineOnly: Boolean = false,
  val offlineOnlyChecked: Boolean = false,
  val editingItem: Pair<Int, BuilderItem<*>>? = null,
  val isFromFile: Boolean = false,
  val hasSameInDb: Boolean = false,
  val is24HourFormat: Boolean = true,
  val hapticFeedbackEnabled: Boolean = true,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
)
