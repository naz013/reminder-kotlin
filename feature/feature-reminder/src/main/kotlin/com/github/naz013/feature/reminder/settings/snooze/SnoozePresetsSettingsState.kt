package com.github.naz013.feature.reminder.settings.snooze

import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import org.threeten.bp.LocalTime

data class SnoozePresetsSettingsState(
  val enabledPresets: Set<SnoozePresetType> = emptySet(),
  val eveningTimeText: String = "",
  val morningTimeText: String = "",
)

enum class SnoozeTimeTarget { EVENING, MORNING }

sealed class SnoozePresetsSettingsEvent {
  data class ShowTimePicker(
    val target: SnoozeTimeTarget,
    val time: LocalTime,
    val title: String,
    val is24Hour: Boolean,
  ) : SnoozePresetsSettingsEvent()
}
