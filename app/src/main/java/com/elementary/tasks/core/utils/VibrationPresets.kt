package com.elementary.tasks.core.utils

import androidx.annotation.StringRes
import com.elementary.tasks.R

data class VibrationPreset(
  @StringRes val nameRes: Int,
  val pattern: List<Long>,
)

/** Named vibration-pattern presets shared by Settings, the reminder builder, and the group editor
 *  notification-override UI, so all three sites offer the same finite set of patterns rather than
 *  a free-form millisecond-array editor (nothing like that exists anywhere in the app). */
object VibrationPresets {
  val ALL = listOf(
    VibrationPreset(R.string.vibration_preset_short, listOf(0, 250)),
    VibrationPreset(R.string.vibration_preset_long, listOf(0, 800)),
    VibrationPreset(R.string.vibration_preset_double_buzz, listOf(0, 200, 150, 200)),
    // The pattern ReminderDataProvider.getVibrationPattern() already hardcodes for every
    // notification today - the one pattern actually in use at delivery time.
    VibrationPreset(R.string.vibration_preset_default, listOf(150, 400, 100, 450, 200, 500, 300, 500)),
  )
}
