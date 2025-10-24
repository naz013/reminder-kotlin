package com.elementary.tasks.calendar.dayview

import com.elementary.tasks.calendar.dayview.weekheader.WeekDay

data class DayViewState(
  val title: String,
  val days: List<WeekDay>
)
