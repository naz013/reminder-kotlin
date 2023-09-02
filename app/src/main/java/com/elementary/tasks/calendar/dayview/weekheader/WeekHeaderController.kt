package com.elementary.tasks.calendar.dayview.weekheader

import org.threeten.bp.LocalDate

class WeekHeaderController(
  private val weekFactory: WeekFactory
) {

  private var startDate: LocalDate? = null
  private var endDate: LocalDate? = null
  private var week: List<WeekDay> = emptyList()

  fun calculateWeek(date: LocalDate): List<WeekDay> {
    return if (isInCurrentRange(date)) {
      week.map {
        it.copy(isSelected = it.localDate == date)
      }
    } else {
      weekFactory.createWeek(date).also {
        week = it
        startDate = week.firstOrNull()?.localDate
        endDate = week.lastOrNull()?.localDate
      }
    }
  }

  private fun isInCurrentRange(date: LocalDate): Boolean {
    if (startDate == null || endDate == null) {
      return false
    }
    return date >= startDate && date <= endDate
  }
}
