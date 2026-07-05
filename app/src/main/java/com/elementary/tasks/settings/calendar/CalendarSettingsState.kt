package com.elementary.tasks.settings.calendar

data class CalendarSettingsState(
  val firstDayName: String = "",
  val todayColorIndex: Int = 0,
  val reminderColorIndex: Int = 0,
  val birthdayColorIndex: Int = 0,
  val selectedCalendarName: String = "",
  val isCalendarSelected: Boolean = false,
  val isExportChecked: Boolean = false,
  val isScanChecked: Boolean = false,
  val dialog: CalendarSettingsDialog? = null,
)

sealed class CalendarSettingsDialog {
  data class FirstDay(val options: List<String>, val selectedIndex: Int) : CalendarSettingsDialog()
}

enum class ColorPickerTarget { TODAY, REMINDER, BIRTHDAY }

sealed class CalendarSettingsEvent {
  data class ShowColorPicker(
    val target: ColorPickerTarget,
    val currentColorIndex: Int,
    val title: String,
  ) : CalendarSettingsEvent()
}
