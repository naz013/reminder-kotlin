package com.github.naz013.feature.reminder.settings

import org.threeten.bp.LocalTime

data class RemindersSettingsState(
  val priorityName: String = "",
  val isCompletedChecked: Boolean = false,
  val isWearChecked: Boolean = false,
  val snoozeText: String = "",
  val isRepeatChecked: Boolean = false,
  val repeatIntervalText: String = "",
  val isRepeatIntervalRowEnabled: Boolean = false,
  val isLedVisible: Boolean = false,
  val isLedChecked: Boolean = false,
  val ledColorName: String = "",
  val isLedColorRowEnabled: Boolean = false,
  val isPermanentNotificationChecked: Boolean = false,
  val isStatusIconChecked: Boolean = false,
  val isStatusIconRowEnabled: Boolean = false,
  val isDoNotDisturbChecked: Boolean = false,
  val doNotDisturbFromText: String = "",
  val doNotDisturbToText: String = "",
  val doNotDisturbActionName: String = "",
  val doNotDisturbIgnoreName: String = "",
  val isDoNotDisturbDependentEnabled: Boolean = false,
  val isDefaultVibrateChecked: Boolean = false,
  val isDefaultBypassDoNotDisturbChecked: Boolean = false,
  val isDefaultWakeScreenChecked: Boolean = false,
  val isDefaultSwipeToDismissChecked: Boolean = false,
  val isInAppAlertBannerChecked: Boolean = false,
  val defaultCategoryName: String = "",
  val defaultLockScreenVisibilityName: String = "",
  val defaultVibrationPatternName: String = "",
  val dialog: RemindersSettingsDialog? = null,
  val hasLocation: Boolean = false,
  val workflowsVisible: Boolean = false,
  val isInsightsLocked: Boolean = false,
)

enum class ChoiceDialogKind {
  PRIORITY, LED_COLOR, DND_ACTION, DND_IGNORE, CATEGORY, LOCK_SCREEN_VISIBILITY, VIBRATION_PATTERN
}

enum class SeekDialogKind { SNOOZE, REPEAT_INTERVAL }

enum class DndTimeTarget { FROM, TO }

sealed class RemindersSettingsDialog {
  data class Choice(
    val kind: ChoiceDialogKind,
    val title: String,
    val options: List<String>,
    val selectedIndex: Int,
  ) : RemindersSettingsDialog()

  data class Seek(
    val kind: SeekDialogKind,
    val title: String,
    val previewValue: Int,
    val formattedValue: String,
  ) : RemindersSettingsDialog()
}

sealed class RemindersSettingsEvent {
  data object OpenPresets : RemindersSettingsEvent()

  data object OpenLocationSettings : RemindersSettingsEvent()

  data object OpenWorkflowRules : RemindersSettingsEvent()

  data class ShowTimePicker(
    val target: DndTimeTarget,
    val time: LocalTime,
    val title: String,
    val is24Hour: Boolean,
  ) : RemindersSettingsEvent()

  data object ShowPermanentNotification : RemindersSettingsEvent()

  data object HidePermanentNotification : RemindersSettingsEvent()

  data object HapticFeedback : RemindersSettingsEvent()
}
