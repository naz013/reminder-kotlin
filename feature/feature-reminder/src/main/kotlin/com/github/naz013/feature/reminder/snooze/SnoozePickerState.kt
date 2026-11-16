package com.github.naz013.feature.reminder.snooze

import com.github.naz013.logic.reminder.snooze.SnoozePresetType

internal data class SnoozePickerState(
  val step: SnoozePickerStep = SnoozePickerStep.Presets,
  val presets: List<SnoozePresetType> = emptyList(),
  val customOptions: List<CustomSnoozeOption> = emptyList(),
)

internal enum class SnoozePickerStep {
  Presets,
  CustomTime,
}

internal data class CustomSnoozeOption(
  val minutes: Int,
  val unit: CustomSnoozeUnit,
  val amount: Int,
)

internal enum class CustomSnoozeUnit {
  Minutes,
  Hours,
  Days,
}
