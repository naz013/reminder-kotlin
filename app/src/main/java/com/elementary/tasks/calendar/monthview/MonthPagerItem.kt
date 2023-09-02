package com.elementary.tasks.calendar.monthview

import org.threeten.bp.LocalDate

data class MonthPagerItem(
  val monthValue: Int,
  val year: Int,
  val date: LocalDate
)
