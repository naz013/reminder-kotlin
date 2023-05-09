package com.elementary.tasks.core.utils.datetime

class DateValidator {

  fun isLegacyMonthValid(month: Int): Boolean {
    return month in 0..11
  }

  fun isMonthValid(month: Int): Boolean {
    return month in 1..12
  }
}
