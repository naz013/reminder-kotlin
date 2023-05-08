package com.elementary.tasks.core.protocol

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
