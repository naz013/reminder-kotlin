package com.elementary.tasks.reminder.scheduling.recurrence

import com.elementary.tasks.core.protocol.WeekDaysProtocol
import com.github.naz013.common.datetime.plusMillis
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class RecurrenceCalculator {

  /**
   * Calculates the next occurrence of a yearly event based on the specified month and day.
   * if the day of month is 0, then it means the last day of the month.
   * If the day of month is greater than 28, it will adjust to the last valid day of the month.
   * Example: if the day of month is 31, and the next month is February, it will set the day to 28 or 29 depending on leap year.
   *
   * @param eventDateTime The original event date and time
   * @param monthOfYear The month of the year for the next occurrence (0 = January, 11 = December)
   * @param dayOfMonth The day of the month for the next occurrence
   * @param interval The interval in years to add
   * @return The calculated next occurrence date and time
   */
  fun getNextYearDayDateTime(
    eventDateTime: LocalDateTime,
    monthOfYear: Int,
    dayOfMonth: Int,
    interval: Long,
  ): LocalDateTime {
    val interval = if (interval <= 0) 1L else interval
    val monthOfYear = monthOfYear + 1
    var nextDateTime = eventDateTime.withDayOfMonth(1).withMonth(monthOfYear).plusYears(interval)
    val lastDayOfTargetMonth = nextDateTime.toLocalDate().lengthOfMonth()
    val targetDay = when {
      dayOfMonth == 0 -> lastDayOfTargetMonth
      dayOfMonth > lastDayOfTargetMonth -> lastDayOfTargetMonth
      else -> dayOfMonth
    }
    nextDateTime = nextDateTime.withDayOfMonth(targetDay)
    return nextDateTime
  }

  /**
   * Finds the next occurrence of a yearly event based on the specified month and day,
   * ensuring that the returned date/time is after or equal to a given date/time.
   *
   * @param eventDateTime The original event date and time
   * @param monthOfYear The month of the year for the next occurrence (0 = January, 11 = December)
   * @param dayOfMonth The day of the month for the next occurrence
   * @param interval The interval in years to add
   * @param afterOrEqualDateTime The date/time that the next occurrence must be after or equal to
   * @return The calculated next occurrence date and time
   */
  fun findNextYearDayDateTime(
    eventDateTime: LocalDateTime,
    monthOfYear: Int,
    dayOfMonth: Int,
    interval: Long,
    afterOrEqualDateTime: LocalDateTime
  ): LocalDateTime {
    var nextDateTime = getNextYearDayDateTime(eventDateTime, monthOfYear, dayOfMonth, interval)
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = getNextYearDayDateTime(nextDateTime, monthOfYear, dayOfMonth, interval)
    }
    return nextDateTime
  }

  /**
   * Calculates the next occurrence of a weekly event based on the specified weekdays.
   *
   * @param eventDateTime The original event date and time
   * @param weekdays The list of selected weekdays (1 = Sunday, 7 = Saturday)
   * @return The calculated next occurrence date and time
   */
  fun getNextDayOfWeekDateTime(
    eventDateTime: LocalDateTime,
    weekdays: List<Int>,
  ): LocalDateTime {
    val selectedDaysOfWeek = WeekDaysProtocol.getSelectedDaysOfWeek(weekdays)
    var nextDateTime = eventDateTime.plusDays(1)
    while (!selectedDaysOfWeek.contains(nextDateTime.dayOfWeek)) {
      nextDateTime = nextDateTime.plusDays(1)
    }
    return nextDateTime
  }

  /**
   * Finds the next occurrence of a weekly event based on the specified weekdays,
   * ensuring that the returned date/time is after or equal to a given date/time.
   *
   * @param eventDateTime The original event date and time
   * @param weekdays The list of selected weekdays (1 = Sunday, 7 = Saturday)
   * @param afterOrEqualDateTime The date/time that the next occurrence must be after or equal to
   * @return The calculated next occurrence date and time
   */
  fun findNextDayOfWeekDateTime(
    eventDateTime: LocalDateTime,
    weekdays: List<Int>,
    afterOrEqualDateTime: LocalDateTime
  ): LocalDateTime {
    var nextDateTime = getNextDayOfWeekDateTime(eventDateTime, weekdays)
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = getNextDayOfWeekDateTime(nextDateTime, weekdays)
    }
    return nextDateTime
  }

  /**
   * Calculates the next occurrence of a monthly event based on the specified day of the month.
   * if the day of month is 0, then it means the last day of the month.
   * If the day of month is greater than 28, it will adjust to the last valid day of the month.
   * Example: if the day of month is 31, and the next month is February, it will set the day to 28 or 29 depending on leap year.
   *
   * @param eventDateTime The original event date and time
   * @param dayOfMonth The day of the month for the next occurrence
   * @param interval The interval in months to add
   * @return The calculated next occurrence date and time
   */
  fun getNextMonthDayDateTime(
    eventDateTime: LocalDateTime,
    dayOfMonth: Int,
    interval: Long,
  ): LocalDateTime {
    val interval = if (interval <= 0) 1L else interval
    var nextDateTime = eventDateTime.withDayOfMonth(1).plusMonths(interval)
    val lastDayOfNextMonth = nextDateTime.toLocalDate().lengthOfMonth()
    val targetDay = when {
      dayOfMonth == 0 -> lastDayOfNextMonth
      dayOfMonth > lastDayOfNextMonth -> lastDayOfNextMonth
      else -> dayOfMonth
    }
    nextDateTime = nextDateTime.withDayOfMonth(targetDay)
    return nextDateTime
  }

  /**
   * Finds the next occurrence of a monthly event based on the specified day of the month,
   * ensuring that the returned date/time is after or equal to a given date/time.
   *
   * @param eventDateTime The original event date and time
   * @param dayOfMonth The day of the month for the next occurrence
   * @param afterOrEqualDateTime The date/time that the next occurrence must be after or equal to
   * @return The calculated next occurrence date and time
   */
  fun findNextMonthDayDateTime(
    eventDateTime: LocalDateTime,
    dayOfMonth: Int,
    interval: Long,
    afterOrEqualDateTime: LocalDateTime
  ): LocalDateTime {
    var nextDateTime = getNextMonthDayDateTime(eventDateTime, dayOfMonth, interval)
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = getNextMonthDayDateTime(nextDateTime, dayOfMonth, interval)
    }
    return nextDateTime
  }

  /**
   * Calculates the next occurrence of an event based on a specified interval.
   *
   * @param eventDateTime The original event date and time
   * @param intervalMillis The interval in milliseconds to add
   * @return The calculated next occurrence date and time
   */
  fun getNextIntervalDateTime(
    eventDateTime: LocalDateTime,
    intervalMillis: Long,
  ): LocalDateTime {
    return eventDateTime.plusMillis(intervalMillis)
  }

  /**
   * Finds the next occurrence of an event based on a specified interval,
   * ensuring that the returned date/time is after or equal to a given date/time.
   *
   * @param eventDateTime The original event date and time
   * @param intervalMillis The interval in milliseconds to add for each occurrence
   * @param afterOrEqualDateTime The date/time that the next occurrence must be after or equal to
   * @return The calculated next occurrence date and time
   */
  fun findNextIntervalDateTime(
    eventDateTime: LocalDateTime,
    intervalMillis: Long,
    afterOrEqualDateTime: LocalDateTime
  ): LocalDateTime {
    var nextDateTime = eventDateTime
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = nextDateTime.plusMillis(intervalMillis)
    }
    return nextDateTime
  }

  /**
   * Calculates the start date and time for a countdown timer.
   *
   * @param countdownTimeInMillis The countdown time in milliseconds
   * @return The calculated start date and time
   */
  fun getStartTimerDateTime(
    countdownTimeInMillis: Long,
  ): LocalDateTime {
    return LocalDateTime.now().plusMillis(countdownTimeInMillis)
  }

  /**
   * Calculates the next occurrence of a timer-based event, considering excluded hours and time ranges.
   *
   * @param eventDateTime The original event date and time
   * @param interval The interval in milliseconds to add for each occurrence
   * @param excludedHours List of hours (0-23) to exclude
   * @param excludedFromTime Start time of the excluded range
   * @param excludedToTime End time of the excluded range
   * @return The calculated next occurrence date and time
   */
  fun getNextTimerDateTime(
    eventDateTime: LocalDateTime,
    interval: Long,
    excludedHours: List<Int>,
    excludedFromTime: LocalTime?,
    excludedToTime: LocalTime?
  ): LocalDateTime {
    if (interval <= 0L) {
      throw IllegalArgumentException("Interval must be greater than zero.")
    }
    var nextDateTime = eventDateTime.plusMillis(interval)
    while (excludedHours.contains(nextDateTime.hour) || isBetweenOf(
        nextDateTime.toLocalTime(),
        excludedFromTime,
        excludedToTime
      )) {
      nextDateTime = nextDateTime.plusMillis(interval)
    }
    return nextDateTime
  }

  /**
   * Finds the next occurrence of a timer-based event, ensuring that the returned date/time
   * is after or equal to a given date/time, while respecting excluded hours and time ranges.
   *
   * @param eventDateTime The original event date and time
   * @param interval The interval multiplier for the countdown
   * @param excludedHours List of hours (0-23) to exclude
   * @param excludedFromTime Start time of the excluded range
   * @param excludedToTime End time of the excluded range
   * @param afterOrEqualDateTime The date/time that the next occurrence must be after or equal to
   * @return The calculated next occurrence date and time
   */
  fun findNextTimerDateTime(
    eventDateTime: LocalDateTime,
    interval: Long,
    excludedHours: List<Int>,
    excludedFromTime: LocalTime?,
    excludedToTime: LocalTime?,
    afterOrEqualDateTime: LocalDateTime,
  ): LocalDateTime {
    var nextDateTime = getNextTimerDateTime(
      eventDateTime,
      interval,
      excludedHours,
      excludedFromTime,
      excludedToTime
    )
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = getNextTimerDateTime(
        nextDateTime,
        interval,
        excludedHours,
        excludedFromTime,
        excludedToTime
      )
    }
    return nextDateTime
  }

  /**
   * Checks if a given time is between two other times, considering overnight ranges.
   *
   * @param time The time to check
   * @param start The start time of the range
   * @param end The end time of the range
   * @return True if the time is within the range, false otherwise
   */
  private fun isBetweenOf(
    time: LocalTime,
    start: LocalTime?,
    end: LocalTime?
  ): Boolean {
    if (start == null || end == null) return false
    return if (start <= end) {
      time in start..end
    } else {
      time >= start || time <= end
    }
  }
}
