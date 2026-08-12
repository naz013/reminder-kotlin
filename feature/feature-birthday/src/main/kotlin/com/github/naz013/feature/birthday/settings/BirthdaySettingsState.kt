package com.github.naz013.feature.birthday.settings

import org.threeten.bp.LocalTime

data class BirthdaySettingsState(
  val isReminderChecked: Boolean = false,
  val isDependentEnabled: Boolean = false,
  val daysToBirthday: Int = 0,
  val priorityName: String = "",
  val reminderTime: String = "",
  val isWidgetChecked: Boolean = false,
  val homeDaysText: String = "",
  val isPermanentChecked: Boolean = false,
  val isGlobalChecked: Boolean = false,
  val isLedChecked: Boolean = false,
  val isLedRowEnabled: Boolean = false,
  val ledColorName: String = "",
  val isLedColorRowEnabled: Boolean = false,
  val isLedIndicationVisible: Boolean = true,
  val isUseContactsChecked: Boolean = false,
  val isAutoScanChecked: Boolean = false,
  val isAutoScanRowEnabled: Boolean = false,
  val dialog: BirthdayDialog? = null,
)

sealed class BirthdayDialog {
  data class Priority(
    val title: String,
    val options: List<String>,
    val selectedIndex: Int,
  ) : BirthdayDialog()

  data class LedColor(
    val title: String,
    val options: List<String>,
    val selectedIndex: Int,
  ) : BirthdayDialog()

  data class DaysToBirthday(
    val previewValue: Int,
    val hapticFeedbackEnabled: Boolean,
  ) : BirthdayDialog()

  data class HomeDays(
    val previewValue: Int,
    val hapticFeedbackEnabled: Boolean,
  ) : BirthdayDialog()
}

sealed class BirthdaySettingsEvent {
  data class ShowTimePicker(
    val time: LocalTime,
    val is24Hour: Boolean,
    val title: String,
  ) : BirthdaySettingsEvent()

  data class UpdatePermanentNotificationVisibility(
    val visible: Boolean,
  ) : BirthdaySettingsEvent()
}
