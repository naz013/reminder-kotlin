package com.elementary.tasks.reminder.build.preset

import com.elementary.tasks.core.data.ui.preset.UiPresetList

data class ManagePresetsState(
  val presets: List<UiPresetList> = emptyList(),
)
