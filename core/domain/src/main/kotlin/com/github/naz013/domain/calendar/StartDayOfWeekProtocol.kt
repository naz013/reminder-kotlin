package com.github.naz013.domain.calendar

class StartDayOfWeekProtocol(private val prefsStartDay: Int) {
  fun getForCalendar(): Int {
    return if (prefsStartDay == 0) {
      return 7
    } else {
      prefsStartDay
    }
  }

  fun getForDatePicker(): Int {
    return prefsStartDay + 1
  }
}
