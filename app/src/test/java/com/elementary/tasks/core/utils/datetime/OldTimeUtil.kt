package com.elementary.tasks.core.utils.datetime

import com.elementary.tasks.core.data.models.Reminder
import com.github.naz013.calendarext.addMonths
import com.github.naz013.calendarext.dropMilliseconds
import com.github.naz013.calendarext.dropSeconds
import com.github.naz013.calendarext.getDayOfMonth
import com.github.naz013.calendarext.getHourOfDay
import com.github.naz013.calendarext.getLastDayOfMonth
import com.github.naz013.calendarext.getMinute
import com.github.naz013.calendarext.getSecond
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.setDayOfMonth
import com.github.naz013.calendarext.setMillis
import com.github.naz013.calendarext.setTime
import com.github.naz013.calendarext.toDate
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class OldTimeUtil {

  fun getNextMonthDayTime(reminder: Reminder, fromTime: Long = System.currentTimeMillis()): Long {
    val dayOfMonth = reminder.dayOfMonth
    val beforeValue = reminder.remindBefore

    Timber.d("getNextMonthDayTime: $dayOfMonth, before -> $beforeValue")

    if (dayOfMonth == 0) {
      return getLastMonthDayTime(fromTime, reminder)
    } else if (dayOfMonth > 28) {
      return getSmartMonthDayTime(fromTime, reminder)
    }

    val calendar = calendarFromEventTime(reminder.eventTime, fromTime)
    calendar.setDayOfMonth(dayOfMonth)
    var interval = reminder.repeatInterval.toInt()
    if (interval <= 0) {
      interval = 1
    }
    var isAfter = calendar.timeInMillis - beforeValue > fromTime
    if (calendar.getDayOfMonth() == dayOfMonth && isAfter) {
      calendar.dropSeconds()
      calendar.dropMilliseconds()
      return calendar.timeInMillis
    }
    while (true) {
      calendar.addMonths(interval)
      isAfter = calendar.timeInMillis - beforeValue > fromTime
      if (calendar.getDayOfMonth() == dayOfMonth && isAfter) {
        break
      }
    }
    calendar.dropSeconds()
    calendar.dropMilliseconds()
    return calendar.timeInMillis
  }

  private fun getSmartMonthDayTime(fromTime: Long, reminder: Reminder): Long {
    val dayOfMonth = reminder.dayOfMonth
    val beforeValue = reminder.remindBefore
    val calendar = calendarFromEventTime(reminder.eventTime, fromTime)
    var interval = reminder.repeatInterval.toInt()
    if (interval <= 0) {
      interval = 1
    }
    var lastDay = calendar.getLastDayOfMonth()
    if (dayOfMonth <= lastDay) {
      calendar.setDayOfMonth(dayOfMonth)
    } else {
      calendar.setDayOfMonth(lastDay)
    }
    var isAfter = calendar.timeInMillis - beforeValue > fromTime
    if (isAfter) {
      calendar.dropSeconds()
      calendar.dropMilliseconds()
      return calendar.timeInMillis
    }
    while (true) {
      isAfter = calendar.timeInMillis - beforeValue > fromTime
      if (isAfter) {
        break
      }
      calendar.setDayOfMonth(1)
      calendar.addMonths(interval)
      lastDay = calendar.getLastDayOfMonth()
      if (dayOfMonth <= lastDay) {
        calendar.setDayOfMonth(dayOfMonth)
      } else {
        calendar.setDayOfMonth(lastDay)
      }
    }
    calendar.dropSeconds()
    calendar.dropMilliseconds()
    return calendar.timeInMillis
  }

  private fun getLastMonthDayTime(fromTime: Long, reminder: Reminder): Long {
    val calendar = calendarFromEventTime(reminder.eventTime, fromTime)
    var interval = reminder.repeatInterval.toInt()
    if (interval <= 0) {
      interval = 1
    }
    while (true) {
      calendar.setDayOfMonth(calendar.getLastDayOfMonth())
      if (calendar.timeInMillis - reminder.remindBefore > fromTime) {
        break
      }
      calendar.setDayOfMonth(1)
      calendar.addMonths(interval)
    }
    calendar.dropSeconds()
    calendar.dropMilliseconds()
    return calendar.timeInMillis
  }

  private fun calendarFromEventTime(eventTime: String, fromTime: Long) =
    newCalendar().takeIf { eventTime != "" }?.apply {
      this.setMillis(getDateTimeFromGmt(eventTime))
      val time = DateTimeManager.Time(getHourOfDay(), getMinute(), getSecond())
      this.setMillis(fromTime)
      this.setTime(time.hour, time.minute)
      this.dropSeconds()
      this.dropMilliseconds()
    } ?: droppedCalendar()

  private fun droppedCalendar() = newCalendar().apply {
    this.dropSeconds()
    this.dropMilliseconds()
  }

  fun getDateTimeFromGmt(dateTime: String?): Long {
    if (dateTime.isNullOrEmpty()) return 0
    return try {
      newCalendar(dateTime.toDate(GMT_DATE_FORMAT, TimeZone.getTimeZone(GMT))).timeInMillis
    } catch (e: Throwable) {
      0
    }
  }

  companion object {
    private const val GMT = "GMT"
    private val GMT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
  }
}