package com.github.naz013.feature.reminder.build.preset

import com.github.naz013.feature.reminder.preset.UiPresetList

data class ManagePresetsState(
  val presets: List<UiPresetList> = emptyList(),
)
