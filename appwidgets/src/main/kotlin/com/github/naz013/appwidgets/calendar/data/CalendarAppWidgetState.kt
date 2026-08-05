package com.github.naz013.appwidgets.calendar.data

import androidx.compose.ui.graphics.Color

internal data class CalendarAppWidgetState(
  val widgetId: Int,
  val backgroundColorIndex: Int,
  val monthYearText: String,
  val weekdays: List<String>,
  val days: List<UiCalendarDay>,
  val todayMarkColor: Color,
  val reminderMarkColor: Color,
  val birthdayMarkColor: Color
)
