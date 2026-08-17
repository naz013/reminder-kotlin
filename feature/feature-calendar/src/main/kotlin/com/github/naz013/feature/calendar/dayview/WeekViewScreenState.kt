package com.github.naz013.feature.calendar.dayview

import com.github.naz013.feature.calendar.dayview.weekheader.WeekDay
import org.threeten.bp.LocalDate

data class WeekViewScreenState(
  val title: String = "",
  val days: List<WeekDay> = emptyList(),
  val selectedDate: LocalDate = LocalDate.now(),
)
