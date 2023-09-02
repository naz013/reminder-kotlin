package com.elementary.tasks.calendar.dayview.weekheader

import org.threeten.bp.LocalDate

data class WeekDay(
  val localDate: LocalDate,
  val weekday: String,
  val date: String,
  val isSelected: Boolean
)
