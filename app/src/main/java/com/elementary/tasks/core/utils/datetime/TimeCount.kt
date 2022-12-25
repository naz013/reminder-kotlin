package com.elementary.tasks.core.utils.datetime

import android.text.TextUtils
import com.elementary.tasks.core.data.models.Reminder
import com.github.naz013.calendarext.addMillis
import com.github.naz013.calendarext.addMonths
import com.github.naz013.calendarext.addYear
import com.github.naz013.calendarext.dropMilliseconds
import com.github.naz013.calendarext.dropSeconds
import com.github.naz013.calendarext.getDayOfMonth
import com.github.naz013.calendarext.getDayOfWeek
import com.github.naz013.calendarext.getHourOfDay
import com.github.naz013.calendarext.getLastDayOfMonth
import com.github.naz013.calendarext.getMinute
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.setDayOfMonth
import com.github.naz013.calendarext.setHourOfDay
import com.github.naz013.calendarext.setMillis
import com.github.naz013.calendarext.setMinute
import com.github.naz013.calendarext.setMonth
import com.github.naz013.calendarext.setTime
import timber.log.Timber
import java.util.*

@Deprecated("Use DateTimeManager")
object TimeCount {

  const val SECOND: Long = 1000
  const val MINUTE: Long = 60 * SECOND
  const val HOUR: Long = MINUTE * 60
  private const val HALF_DAY: Long = HOUR * 12
  const val DAY: Long = HALF_DAY * 2
  const val WEEK: Long = DAY * 7

  fun isCurrent(eventTime: String?): Boolean {
    return TimeUtil.getDateTimeFromGmt(eventTime) > System.currentTimeMillis()
  }

  fun isCurrent(millis: Long): Boolean {
    return millis > System.currentTimeMillis()
  }

  fun generateNextTimer(reminder: Reminder, isNew: Boolean): Long {
    val hours = reminder.hours
    val fromHour = reminder.from
    val toHour = reminder.to
    val calendar = if (isNew) {
      newCalendar(System.currentTimeMillis() + reminder.after)
    } else {
      newCalendar(TimeUtil.getDateTimeFromGmt(reminder.eventTime) + reminder.repeatInterval)
    }
    if (hours.isNotEmpty()) {
      while (hours.contains(calendar.getHourOfDay())) {
        calendar.timeInMillis = calendar.timeInMillis + reminder.repeatInterval
      }
      return calendar.timeInMillis
    }
    var eventTime = calendar.timeInMillis
    if (fromHour != "" && toHour != "") {
      val fromDate = TimeUtil.getDate(fromHour)
      val toDate = TimeUtil.getDate(toHour)
      if (fromDate != null && toDate != null) {
        calendar.time = fromDate
        var hour = calendar.getHourOfDay()
        var minute = calendar.getMinute()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.setHourOfDay(hour)
        calendar.setMinute(minute)
        val start = calendar.timeInMillis
        calendar.time = toDate
        hour = calendar.getHourOfDay()
        minute = calendar.getMinute()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.setHourOfDay(hour)
        calendar.setMinute(minute)
        val end = calendar.timeInMillis
        while (isRange(eventTime, start, end)) {
          eventTime += reminder.repeatInterval
        }
      }
    }
    return eventTime
  }

  private fun isRange(time: Long, start: Long, end: Long): Boolean {
    return if (start > end) {
      time >= start || time < end
    } else {
      time in start..end
    }
  }

  fun generateDateTime(eventTime: String, repeat: Long, fromTime: Long = System.currentTimeMillis()): Long {
    return if (TextUtils.isEmpty(eventTime)) {
      0
    } else {
      var time = TimeUtil.getDateTimeFromGmt(eventTime)
      while (time <= fromTime) {
        time += repeat
      }
      time
    }
  }

  fun getNextWeekdayTime(reminder: Reminder, fromTime: Long = System.currentTimeMillis()): Long {
    val weekdays = reminder.weekdays
    val beforeValue = reminder.remindBefore
    val calendar = (if (reminder.eventTime != "") {
      newCalendar(TimeUtil.getDateTimeFromGmt(reminder.eventTime))
    } else droppedCalendar())
    while (true) {
      if (weekdays[calendar.getDayOfWeek() - 1] == 1 && calendar.timeInMillis - beforeValue > fromTime) {
        break
      }
      calendar.addMillis(DAY)
    }
    return calendar.timeInMillis
  }

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
      isAfter = calendar.timeInMillis - beforeValue > fromTime
      if (calendar.getDayOfMonth() == dayOfMonth && isAfter) {
        break
      }
      calendar.addMonths(interval)
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

  fun getNextYearDayTime(reminder: Reminder, fromTime: Long = System.currentTimeMillis()): Long {
    val dayOfMonth = reminder.dayOfMonth
    val monthOfYear = reminder.monthOfYear
    val beforeValue = reminder.remindBefore
    val calendar = calendarFromEventTime(reminder.eventTime, fromTime)
    calendar.setMonth(monthOfYear)
    calendar.setDayOfMonth(with(calendar.getLastDayOfMonth()) {
      if (dayOfMonth <= this) dayOfMonth
      else this
    })
    if (calendar.timeInMillis - beforeValue <= fromTime) {
      while (true) {
        if (calendar.timeInMillis - beforeValue > fromTime) {
          break
        }
        calendar.setDayOfMonth(1)
        calendar.addYear()
        calendar.setDayOfMonth(with(calendar.getLastDayOfMonth()) {
          if (dayOfMonth <= this) dayOfMonth
          else this
        })
      }
    }
    calendar.dropSeconds()
    calendar.dropMilliseconds()
    return calendar.timeInMillis
  }

  private fun calendarFromEventTime(eventTime: String, fromTime: Long) =
    newCalendar().takeIf { eventTime != "" }?.apply {
      this.setMillis(TimeUtil.getDateTimeFromGmt(eventTime))
      val hm = TimeUtil.HM(this.getHourOfDay(), this.getMinute())
      this.setMillis(fromTime)
      this.setTime(hm.hour, hm.minute)
      this.dropSeconds()
      this.dropMilliseconds()
    } ?: droppedCalendar()

  private fun droppedCalendar() = newCalendar().apply {
    this.dropSeconds()
    this.dropMilliseconds()
  }
}
