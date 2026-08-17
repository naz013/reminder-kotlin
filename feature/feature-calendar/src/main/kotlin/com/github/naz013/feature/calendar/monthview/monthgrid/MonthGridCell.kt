package com.github.naz013.feature.calendar.monthview.monthgrid

import org.threeten.bp.LocalDate

data class MonthGridCell(
  val date: LocalDate,
  val isCurrentMonth: Boolean,
  val isToday: Boolean,
)
