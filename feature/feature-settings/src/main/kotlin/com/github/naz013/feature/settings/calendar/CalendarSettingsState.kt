package com.github.naz013.feature.settings.calendar

import androidx.compose.ui.graphics.Color

internal data class CalendarSettingsState(
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

internal sealed class CalendarSettingsDialog {
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
}

internal enum class ColorPickerTarget { TODAY, REMINDER, BIRTHDAY }

internal data class GoogleCalendar(
  val id: Long,
  val name: String?,
)
