package com.elementary.tasks.monthview

import org.threeten.bp.LocalDate

interface MonthCallback {
  fun onDateClick(date: LocalDate)
  fun onDateLongClick(date: LocalDate)
}
