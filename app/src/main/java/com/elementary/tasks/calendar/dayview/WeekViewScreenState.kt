package com.elementary.tasks.calendar.dayview

import com.elementary.tasks.calendar.dayview.weekheader.WeekDay
import org.threeten.bp.LocalDate

data class WeekViewScreenState(
  val title: String = "",
  val days: List<WeekDay> = emptyList(),
  val selectedDate: LocalDate = LocalDate.now(),
)
