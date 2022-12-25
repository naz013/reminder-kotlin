package com.backdoor.engine.lang

import com.backdoor.engine.misc.TimeUtil
import org.threeten.bp.LocalDateTime

const val SECOND = 1000L
const val MINUTE = SECOND * 60
const val HOUR = MINUTE * 60
const val DAY = HOUR * 24

fun getExpectedDateTime(month: Int, day: Int, hour: Int, minute: Int): String {
  val dateTime = LocalDateTime.now()
    .withMonth(month)
    .withDayOfMonth(day)
    .withHour(hour)
    .withMinute(minute)
    .withSecond(0)
  return TimeUtil.getGmtFromDateTime(dateTime)
}

fun getDateTimeWithShiftedYearIfNeeded(month: Int, day: Int, hour: Int, minute: Int): String {
  var dateTime = LocalDateTime.now()
    .withMonth(month)
    .withDayOfMonth(day)
    .withHour(hour)
    .withMinute(minute)
    .withSecond(0)

  if (dateTime.isBefore(LocalDateTime.now())) {
    dateTime = dateTime.plusYears(1)
  }

  return TimeUtil.getGmtFromDateTime(dateTime)
}

val TIMES = listOf(
  "07:00",
  "12:00",
  "19:00",
  "23:00"
)
