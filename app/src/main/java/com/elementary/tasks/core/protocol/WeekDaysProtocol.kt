package com.elementary.tasks.core.protocol

import org.threeten.bp.DayOfWeek

object WeekDaysProtocol {

  /**
   * Converts a list of integers representing selected weekdays into a list of DayOfWeek enums.
   * 1 indicates the day is selected, and 0 indicates it is not.
   * 0 - Sunday
   * 1 - Monday
   * 2 - Tuesday
   * 3 - Wednesday
   * 4 - Thursday
   * 5 - Friday
   * 6 - Saturday
   */
  fun getSelectedDaysOfWeek(weekdays: List<Int>): List<DayOfWeek> {
    return listOfNotNull(
      DayOfWeek.SUNDAY.takeIf { weekdays[0] == 1 },
      DayOfWeek.MONDAY.takeIf { weekdays[1] == 1 },
      DayOfWeek.TUESDAY.takeIf { weekdays[2] == 1 },
      DayOfWeek.WEDNESDAY.takeIf { weekdays[3] == 1 },
      DayOfWeek.THURSDAY.takeIf { weekdays[4] == 1 },
      DayOfWeek.FRIDAY.takeIf { weekdays[5] == 1 },
      DayOfWeek.SATURDAY.takeIf { weekdays[6] == 1 },
    )
  }

  /**
   * Returns a list representing workdays (Monday to Friday).
   * 1 indicates a workday, and 0 indicates a non-workday.
   0 - Sunday
   1 - Monday
   2 - Tuesday
   3 - Wednesday
   4 - Thursday
   5 - Friday
   6 - Saturday
   */
  fun getWorkDays(): List<Int> {
    return listOf(0, 1, 1, 1, 1, 1, 0)
  }

  /**
   * Returns a list representing weekend days (Saturday and Sunday).
   * 1 indicates a weekend day, and 0 indicates a non-weekend day.
   * 0 - Sunday
   * 1 - Monday
   * 2 - Tuesday
   * 3 - Wednesday
   * 4 - Thursday
   * 5 - Friday
   * 6 - Saturday
   */
  fun getWeekend(): List<Int> {
    return listOf(1, 0, 0, 0, 0, 0, 1)
  }

  fun getAllDays(): List<Int> {
    return listOf(1, 1, 1, 1, 1, 1, 1)
  }
}
