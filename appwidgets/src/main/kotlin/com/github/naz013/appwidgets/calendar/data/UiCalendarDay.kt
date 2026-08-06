package com.github.naz013.appwidgets.calendar.data

import org.threeten.bp.LocalDate

internal data class UiCalendarDay(
  val date: LocalDate,
  val dayText: String,
  val isCurrentMonth: Boolean,
  val isToday: Boolean,
  val hasReminder: Boolean,
  val hasBirthday: Boolean
)
