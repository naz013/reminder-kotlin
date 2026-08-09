package com.elementary.tasks.settings.calendar

import androidx.compose.ui.graphics.Color

data class CalendarSettingsState(
  val firstDayName: String = "",
  val todayColor: Color = Color.Unspecified,
  val reminderColor: Color = Color.Unspecified,
  val birthdayColor: Color = Color.Unspecified,
  val selectedCalendarName: String = "",
  val isCalendarSelected: Boolean = false,
  val isExportChecked: Boolean = false,
  val isScanChecked: Boolean = false,
  val isHolidaysEnabled: Boolean = false,
  val holidayCountryLabel: String = "",
  val dialog: CalendarSettingsDialog? = null,
)

sealed class CalendarSettingsDialog {
  data class FirstDay(
    val options: List<String>,
    val selectedIndex: Int,
  ) : CalendarSettingsDialog()

  data class ColorPicker(
    val target: ColorPickerTarget,
    val title: String,
    val selectedIndex: Int,
    val colors: List<Color>,
    val hapticFeedback: Boolean,
  ) : CalendarSettingsDialog()

  data class SelectGoogleCalendar(
    val calendars: List<GoogleCalendar>,
    val selectedPosition: Int,
  ) : CalendarSettingsDialog()

  data class SelectCountry(
    val options: List<String>,
    val selectedIndex: Int,
  ) : CalendarSettingsDialog()
}

enum class ColorPickerTarget { TODAY, REMINDER, BIRTHDAY }

data class GoogleCalendar(
  val id: Long,
  val name: String?,
)

data class CountryOption(
  val code: String,
  val label: String,
)
