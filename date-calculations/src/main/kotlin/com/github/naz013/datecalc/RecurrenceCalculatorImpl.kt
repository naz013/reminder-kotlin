package com.github.naz013.datecalc

import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class RecurrenceCalculatorImpl : RecurrenceCalculator {
  override fun getNextYearDayDateTime(
    eventDateTime: LocalDateTime,
    monthOfYear: Int,
    dayOfMonth: Int,
    interval: Long,
  ): LocalDateTime {
    if (dayOfMonth < 0) {
      throw IllegalArgumentException("dayOfMonth must be non-negative, but was $dayOfMonth")
    }
    val interval = if (interval <= 0) 1L else interval
    val monthOfYear = monthOfYear + 1
    var nextDateTime = eventDateTime.withDayOfMonth(1).withMonth(monthOfYear).plusYears(interval)
    val lastDayOfTargetMonth = nextDateTime.toLocalDate().lengthOfMonth()
    val targetDay =
      when {
        dayOfMonth == 0 -> lastDayOfTargetMonth
        dayOfMonth > lastDayOfTargetMonth -> lastDayOfTargetMonth
        else -> dayOfMonth
      }
    nextDateTime = nextDateTime.withDayOfMonth(targetDay)
    return nextDateTime
  }

  override fun findNextYearDayDateTime(
    eventDateTime: LocalDateTime,
    monthOfYear: Int,
    dayOfMonth: Int,
    interval: Long,
    afterOrEqualDateTime: LocalDateTime,
  ): LocalDateTime {
    var nextDateTime = getNextYearDayDateTime(eventDateTime, monthOfYear, dayOfMonth, interval)
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = getNextYearDayDateTime(nextDateTime, monthOfYear, dayOfMonth, interval)
    }
    return nextDateTime
  }

  override fun getNextDayOfWeekDateTime(
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

  override fun findNextDayOfWeekDateTime(
    eventDateTime: LocalDateTime,
    weekdays: List<Int>,
    afterOrEqualDateTime: LocalDateTime,
  ): LocalDateTime {
    var nextDateTime = getNextDayOfWeekDateTime(eventDateTime, weekdays)
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = getNextDayOfWeekDateTime(nextDateTime, weekdays)
    }
    return nextDateTime
  }

  override fun getNextMonthDayDateTime(
    eventDateTime: LocalDateTime,
    dayOfMonth: Int,
    interval: Long,
  ): LocalDateTime {
    val interval = if (interval <= 0) 1L else interval
    var nextDateTime = eventDateTime.withDayOfMonth(1).plusMonths(interval)
    val lastDayOfNextMonth = nextDateTime.toLocalDate().lengthOfMonth()
    val targetDay =
      when {
        dayOfMonth <= 0 -> lastDayOfNextMonth
        dayOfMonth > lastDayOfNextMonth -> lastDayOfNextMonth
        else -> dayOfMonth
      }
    nextDateTime = nextDateTime.withDayOfMonth(targetDay)
    return nextDateTime
  }

  override fun findNextMonthDayDateTime(
    eventDateTime: LocalDateTime,
    dayOfMonth: Int,
    interval: Long,
    afterOrEqualDateTime: LocalDateTime,
  ): LocalDateTime {
    var nextDateTime = getNextMonthDayDateTime(eventDateTime, dayOfMonth, interval)
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = getNextMonthDayDateTime(nextDateTime, dayOfMonth, interval)
    }
    return nextDateTime
  }

  override fun getNextIntervalDateTime(
    eventDateTime: LocalDateTime,
    intervalMillis: Long,
  ): LocalDateTime = eventDateTime.plusMillis(intervalMillis)

  override fun findNextIntervalDateTime(
    eventDateTime: LocalDateTime,
    intervalMillis: Long,
    afterOrEqualDateTime: LocalDateTime,
  ): LocalDateTime {
    var nextDateTime = eventDateTime
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime = nextDateTime.plusMillis(intervalMillis)
    }
    return nextDateTime
  }

  override fun getStartTimerDateTime(countdownTimeInMillis: Long): LocalDateTime = LocalDateTime.now().plusMillis(countdownTimeInMillis)

  override fun getNextTimerDateTime(
    eventDateTime: LocalDateTime,
    interval: Long,
    excludedHours: List<Int>,
    excludedFromTime: LocalTime?,
    excludedToTime: LocalTime?,
  ): LocalDateTime {
    if (interval <= 0L) {
      throw IllegalArgumentException("Interval must be greater than zero.")
    }
    var nextDateTime = eventDateTime.plusMillis(interval)
    while (excludedHours.contains(nextDateTime.hour) ||
      isBetweenOf(
        nextDateTime.toLocalTime(),
        excludedFromTime,
        excludedToTime,
      )
    ) {
      nextDateTime = nextDateTime.plusMillis(interval)
    }
    return nextDateTime
  }

  override fun findNextTimerDateTime(
    eventDateTime: LocalDateTime,
    interval: Long,
    excludedHours: List<Int>,
    excludedFromTime: LocalTime?,
    excludedToTime: LocalTime?,
    afterOrEqualDateTime: LocalDateTime,
  ): LocalDateTime {
    var nextDateTime =
      getNextTimerDateTime(
        eventDateTime,
        interval,
        excludedHours,
        excludedFromTime,
        excludedToTime,
      )
    while (nextDateTime.isBefore(afterOrEqualDateTime)) {
      nextDateTime =
        getNextTimerDateTime(
          nextDateTime,
          interval,
          excludedHours,
          excludedFromTime,
          excludedToTime,
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
    end: LocalTime?,
  ): Boolean {
    if (start == null || end == null) return false
    return if (start <= end) {
      time in start..end
    } else {
      time >= start || time <= end
    }
  }
}
