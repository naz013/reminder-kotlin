package com.elementary.tasks.core.utils.datetime

import android.app.AlarmManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.map
import com.elementary.tasks.core.utils.params.Prefs
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
import com.github.naz013.calendarext.getMonth
import com.github.naz013.calendarext.getSecond
import com.github.naz013.calendarext.getYear
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.setDayOfMonth
import com.github.naz013.calendarext.setHourOfDay
import com.github.naz013.calendarext.setMillis
import com.github.naz013.calendarext.setMinute
import com.github.naz013.calendarext.setMonth
import com.github.naz013.calendarext.setTime
import com.github.naz013.calendarext.toCalendar
import com.github.naz013.calendarext.toDate
import com.github.naz013.calendarext.toDateWithException
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DateTimeManager(
  private val prefs: Prefs,
  private val textProvider: TextProvider
) {

  fun formatBirthdayDate(date: LocalDate): String {
    return date.format(BIRTH_DATE_FORMATTER)
  }

  fun parseBirthdayDate(date: String): LocalDate {
    return LocalDate.parse(date, BIRTH_DATE_FORMATTER)
  }

  fun fromMillis(millis: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
  }

  fun isAfterNow(gmt: String?): Boolean {
    return try {
      gmtToLocal(gmt, DateTimeFormatter.ofPattern(FIRE_DATE_PATTERN, Locale.US))
        ?.isAfter(LocalDateTime.now()) ?: false
    } catch (e: Throwable) {
      false
    }
  }

  fun getFireFormatted(gmt: String?): String? {
    return gmtToLocal(gmt, DateTimeFormatter.ofPattern(FIRE_DATE_PATTERN, Locale.US))
      ?.let { getDateTime(it) }
  }

  private fun gmtToLocal(gmt: String?, pattern: String): LocalDateTime? {
    return gmtToLocal(gmt, DateTimeFormatter.ofPattern(pattern))
  }

  private fun gmtToLocal(gmt: String?, formatter: DateTimeFormatter): LocalDateTime? {
    return if (gmt == null) {
      null
    } else {
      ZonedDateTime.parse(gmt, formatter)
        .withZoneSameInstant(ZoneId.systemDefault())
        .toLocalDateTime()
    }
  }

  fun millisToEndDnd(from: String?, to: String?, current: Long): Long {
    return doNotDisturbRange(from, to).last - current
  }

  fun doNotDisturbRange(from: String?, to: String?): LongRange {
    var fromMillis = 0L
    var toMillis = 0L
    if (from != null) {
      fromMillis = toMillis(from)
    }
    if (to != null) {
      toMillis = toMillis(to)
    }
    val fromHm = hourMinute(fromMillis)
    val toHm = hourMinute(toMillis)
    Timber.d("doNotDisturbRange: HM $fromHm, $toHm")
    val compare = compareHm(fromHm, toHm)
    if (compare < 0) {
      if (toMillis < fromMillis) {
        toMillis += AlarmManager.INTERVAL_DAY
      }
    } else if (compare == 0) {
      return LongRange(0, 0)
    }
    Timber.d("doNotDisturbRange: millis $fromMillis, $toMillis")
    return if (fromMillis > toMillis) {
      LongRange(toMillis, fromMillis)
    } else {
      LongRange(fromMillis, toMillis)
    }
  }

  private fun compareHm(from: Pair<Int, Int>, to: Pair<Int, Int>): Int {
    return when {
      from.first == to.first -> when {
        from.second == to.second -> 0
        from.second > to.second -> -1
        else -> 1
      }
      from.first > to.first -> -1
      else -> 1
    }
  }

  private fun hourMinute(millis: Long): Pair<Int, Int> {
    return newCalendar(millis).map { Pair(it.getHourOfDay(), it.getMinute()) }
  }

  fun getMillisFromGmtVoiceEngine(dateTime: String?): Long {
    if (dateTime.isNullOrEmpty()) return 0
    return try {
      ZonedDateTime.parse(
        dateTime,
        VOICE_ENGINE_GMT_DATE_FORMAT.withZone(GMT_ZONE_ID)
      ).toInstant().toEpochMilli()
    } catch (e: Exception) {
      e.printStackTrace()
      0
    }
  }

  fun fromGmtToLocal(dateTime: String?, def: LocalDateTime = LocalDateTime.now()): LocalDateTime {
    if (dateTime.isNullOrEmpty()) return def
    return try {
      gmtToLocal(dateTime, GMT_DATE_FORMATTER) ?: def
    } catch (e: Throwable) {
      def
    }
  }

  fun getGmtFromDateTime(dateTime: LocalDateTime): String {
    return try {
      dateTime.atZone(ZoneId.systemDefault()).format(GMT_DATE_FORMATTER.withZone(ZoneId.of(GMT)))
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
    }
  }

  fun toMillis(localDateTime: LocalDateTime): Long {
    return ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli()
  }

  fun toTime(localTime: LocalTime): String {
    return getTime(localTime)
  }

  fun toGoogleTaskDate(localDate: LocalDate): String {
    return localDate.format(fullDateFormatter())
  }

  fun getDate(date: java.util.Date, format: DateFormat): String {
    format.timeZone = TimeZone.getDefault()
    return format.format(date)
  }

  fun getDate(date: Long): String {
    return date().format(newCalendar(date).time)
  }

  fun getDate(date: String): java.util.Date? {
    return try {
      TIME_24.parse(date)
    } catch (e: Throwable) {
      null
    }
  }

  fun logDateTime(millis: Long = System.currentTimeMillis()): String {
    return fullDateTime24().format(newCalendar(millis).time)
  }

  fun getDateTimeFromGmt(dateTime: String?): Long {
    if (dateTime.isNullOrEmpty()) return 0
    return try {
      newCalendar(dateTime.toDate(GMT_DATE_FORMAT, TimeZone.getTimeZone(GMT))).timeInMillis
    } catch (e: Throwable) {
      0
    }
  }

  fun getNextDateTime(millis: Long): Array<String> {
    return if (millis == 0L) {
      arrayOf("", "")
    } else {
      val date = Date(millis)
      arrayOf(
        date().format(date),
        getTime(date)
      )
    }
  }

  fun getRemaining(dateTime: String?, delay: Int): String {
    if (dateTime.isNullOrEmpty()) {
      return getRemaining(0)
    }
    val time = getDateTimeFromGmt(dateTime)
    return getRemaining(time + delay * MINUTE)
  }

  fun getRemaining(eventTime: Long): String {
    val difference = eventTime - System.currentTimeMillis()
    val days = difference / DAY
    var hours = (difference - DAY * days) / HOUR
    var minutes = (difference - DAY * days - HOUR * hours) / MINUTE
    hours = if (hours < 0) -hours else hours
    val result = StringBuilder()
    val language = Language.getScreenLanguage(prefs.appLanguage).toString().lowercase()
    if (difference > DAY) {
      if (language.startsWith("uk")) {
        var last = days
        while (last > 10) {
          last -= 10
        }
        if (last == 1L && days != 11L) {
          result.append(textProvider.getText(R.string.x_day, days.toString()))
        } else if (last < 5 && (days < 12 || days > 14)) {
          result.append(textProvider.getText(R.string.x_dayzz, days.toString()))
        } else {
          result.append(textProvider.getText(R.string.x_days, days.toString()))
        }
      } else {
        if (days < 2) {
          result.append(textProvider.getText(R.string.x_day, days.toString()))
        } else {
          result.append(textProvider.getText(R.string.x_days, days.toString()))
        }
      }
    } else if (difference > HOUR) {
      hours += days * 24
      if (language.startsWith("uk")) {
        var last = hours
        while (last > 10) {
          last -= 10
        }
        if (last == 1L && hours != 11L) {
          result.append(textProvider.getText(R.string.x_hour, hours.toString()))
        } else if (last < 5 && (hours < 12 || hours > 14)) {
          result.append(textProvider.getText(R.string.x_hourzz, hours.toString()))
        } else {
          result.append(textProvider.getText(R.string.x_hours, hours.toString()))
        }
      } else {
        if (hours < 2) {
          result.append(textProvider.getText(R.string.x_hour, hours.toString()))
        } else {
          result.append(textProvider.getText(R.string.x_hours, hours.toString()))
        }
      }
    } else if (difference > MINUTE) {
      minutes += hours * 60
      if (language.startsWith("uk")) {
        var last = minutes
        while (last > 10) {
          last -= 10
        }
        if (last == 1L && minutes != 11L) {
          result.append(textProvider.getText(R.string.x_minute, minutes.toString()))
        } else if (last < 5 && (minutes < 12 || minutes > 14)) {
          result.append(textProvider.getText(R.string.x_minutezz, minutes.toString()))
        } else {
          result.append(textProvider.getText(R.string.x_minutes, minutes.toString()))
        }
      } else {
        if (hours < 2) {
          result.append(textProvider.getText(R.string.x_minute, minutes.toString()))
        } else {
          result.append(textProvider.getText(R.string.x_minutes, minutes.toString()))
        }
      }
    } else if (difference > 0) {
      result.append(textProvider.getText(R.string.less_than_minute))
    } else {
      result.append(textProvider.getText(R.string.overdue))
    }
    return result.toString()
  }

  fun getFullDateTime(date: Long): String {
    val calendar = newCalendar(date)
    return if (prefs.is24HourFormat) {
      fullDateTime24().format(calendar.time)
    } else {
      fullDateTime12().format(calendar.time)
    }
  }

  fun getFutureBirthdayDate(birthdayTime: Long, fullDate: String): BirthDate {
    return fullDate.toDate(BIRTH_DATE_FORMAT).let { date ->
      val calendar = newCalendar(date)
      val bDay = calendar.get(Calendar.DAY_OF_MONTH)
      val bMonth = calendar.get(Calendar.MONTH)
      val year = calendar.get(Calendar.YEAR)
      calendar.timeInMillis = birthdayTime
      val hour = calendar.get(Calendar.HOUR_OF_DAY)
      val minute = calendar.get(Calendar.MINUTE)
      calendar.timeInMillis = System.currentTimeMillis()
      calendar.set(Calendar.MONTH, bMonth)
      calendar.set(Calendar.DAY_OF_MONTH, bDay)
      calendar.set(Calendar.HOUR_OF_DAY, hour)
      calendar.set(Calendar.MINUTE, minute)
      if (calendar.timeInMillis < System.currentTimeMillis()) {
        calendar.add(Calendar.YEAR, 1)
      }
      BirthDate(calendar.timeInMillis, year)
    }
  }

  fun getReadableBirthDate(dateOfBirth: String?): String {
    if (dateOfBirth.isNullOrEmpty()) return ""
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return try {
      dateOfBirth.toDate(format).let { date().format(it) }
    } catch (e: Throwable) {
      dateOfBirth
    }
  }

  fun getBirthdayTime(): Long {
    val time = prefs.birthdayTime
    var millis = toMillis(time)
    if (millis < System.currentTimeMillis()) {
      millis += AlarmManager.INTERVAL_DAY
    }
    return millis
  }

  fun getAgeFormatted(date: String?): String {
    val years = getAge(date)
    val language = Language.getScreenLanguage(prefs.appLanguage).language.lowercase()
    return buildYearString(language, years)
  }

  fun getAgeFormatted(
    yearOfBirth: Int,
    at: Long = System.currentTimeMillis()
  ): String {
    val years = getAge(yearOfBirth, at)
    val language = Language.getScreenLanguage(prefs.appLanguage).toString().lowercase()
    return buildYearString(language, years)
  }

  private fun buildYearString(language: String, years: Int): String {
    val result = StringBuilder()
    if (language.startsWith("uk") || language.startsWith("ru")) {
      var last = years.toLong()
      while (last > 10) {
        last -= 10
      }
      if (last == 1L && years != 11) {
        result.append(textProvider.getText(R.string.x_year, years.toString()))
      } else if (last < 5 && (years < 12 || years > 14)) {
        result.append(textProvider.getText(R.string.x_yearzz, years.toString()))
      } else {
        result.append(textProvider.getText(R.string.x_years, years.toString()))
      }
    } else {
      if (years < 2) {
        result.append(textProvider.getText(R.string.x_year, years.toString()))
      } else {
        result.append(textProvider.getText(R.string.x_years, years.toString()))
      }
    }
    return result.toString()
  }

  fun getDayStart(millis: Long = System.currentTimeMillis()): String {
    return newCalendar(millis).apply {
      this.setHourOfDay(0)
      this.setMinute(0)
      this.dropSeconds()
      this.dropMilliseconds()
    }.map { getGmtFromDateTime(it.timeInMillis) }
  }

  fun getDayEnd(millis: Long = System.currentTimeMillis()): String {
    return getDayStart(millis + AlarmManager.INTERVAL_DAY)
  }

  fun getBirthdayDayMonthList(
    start: Long = System.currentTimeMillis(),
    duration: Int = 1
  ): List<String> {
    val list = mutableListOf<String>()
    val calendar = newCalendar()
    for (n in 0 until duration) {
      calendar.timeInMillis = start + (AlarmManager.INTERVAL_DAY * n)
      list.add("${calendar.getDayOfMonth()}|${calendar.getMonth()}")
    }
    Timber.d("getBirthdayDayMonthList: $list")
    return list
  }

  fun getGmtFromDateTime(date: Long): String {
    GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
    return try {
      GMT_DATE_FORMAT.format(Date(date))
    } catch (e: Throwable) {
      ""
    }
  }

  private fun toMillis(time24: String): Long {
    return try {
      time24.toDateWithException(TIME_24, TimeZone.getDefault()).toCalendar().let {
        val hour = it.getHourOfDay()
        val minute = it.getMinute()
        it.timeInMillis = System.currentTimeMillis()
        it.setHourOfDay(hour)
        it.setMinute(minute)
        it.dropSeconds()
        it.dropMilliseconds()
        it.timeInMillis
      }
    } catch (e: Throwable) {
      0
    }
  }

  private fun getAge(year: Int, at: Long = System.currentTimeMillis()): Int {
    return newCalendar(at).getYear() - year
  }

  fun getAge(dateOfBirth: String?): Int {
    if (dateOfBirth.isNullOrEmpty()) return 0
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return newCalendar().getYear() - newCalendar(dateOfBirth.toDate(format)).getYear()
  }

  fun getRepeatString(repCode: List<Int>): String {
    val sb = StringBuilder()
    val first = prefs.startDay
    if (first == 0 && repCode[0] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sun))
    }
    if (repCode[1] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.mon))
    }
    if (repCode[2] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.tue))
    }
    if (repCode[3] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.wed))
    }
    if (repCode[4] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.thu))
    }
    if (repCode[5] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.fri))
    }
    if (repCode[6] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sat))
    }
    if (first == 1 && repCode[0] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sun))
    }
    return if (isAllChecked(repCode)) {
      textProvider.getText(R.string.everyday)
    } else {
      sb.toString().trim()
    }
  }

  fun isAllChecked(repCode: List<Int>): Boolean {
    return repCode.none { it == 0 }
  }

  private fun localizedDateFormat(pattern: String): SimpleDateFormat {
    return SimpleDateFormat(pattern, Language.getScreenLanguage(prefs.appLanguage))
  }

  private fun localizedDateFormatter(pattern: String): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(pattern, Language.getScreenLanguage(prefs.appLanguage))
  }

  fun getTime(date: java.util.Date): String {
    return if (prefs.is24HourFormat) {
      time24().format(date)
    } else {
      time12().format(date)
    }
  }

  fun getTime(time: LocalTime): String {
    return if (prefs.is24HourFormat) {
      time.format(time24Formatter())
    } else {
      time.format(time12Formatter())
    }
  }

  fun getTime(millis: Long): String {
    return getTime(newCalendar(millis).time)
  }

  fun isCurrent(eventTime: String?): Boolean {
    return getDateTimeFromGmt(eventTime) > System.currentTimeMillis()
  }

  fun isCurrent(millis: Long): Boolean {
    return millis > System.currentTimeMillis()
  }

  fun isCurrent(dateTime: LocalDateTime): Boolean {
    return dateTime.isAfter(LocalDateTime.now())
  }

  fun getDateTime(date: java.util.Date): String {
    return if (prefs.is24HourFormat) {
      dateTime24().format(date)
    } else {
      dateTime12().format(date)
    }
  }

  fun getDateTime(dateTime: LocalDateTime): String {
    return if (prefs.is24HourFormat) {
      dateTime.format(dateTime24Formatter())
    } else {
      dateTime.format(dateTime12Formatter())
    }
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

  fun generateDateTime(
    eventTime: String,
    repeat: Long,
    fromTime: Long = System.currentTimeMillis()
  ): Long {
    return if (eventTime.isEmpty()) {
      0
    } else {
      var time = getDateTimeFromGmt(eventTime)
      while (time <= fromTime) {
        time += repeat
      }
      time
    }
  }

  fun generateNextTimer(reminder: Reminder, isNew: Boolean): Long {
    val hours = reminder.hours
    val fromHour = reminder.from
    val toHour = reminder.to
    val calendar = if (isNew) {
      newCalendar(System.currentTimeMillis() + reminder.after)
    } else {
      newCalendar(getDateTimeFromGmt(reminder.eventTime) + reminder.repeatInterval)
    }
    if (hours.isNotEmpty()) {
      while (hours.contains(calendar.getHourOfDay())) {
        calendar.timeInMillis = calendar.timeInMillis + reminder.repeatInterval
      }
      return calendar.timeInMillis
    }
    var eventTime = calendar.timeInMillis
    if (fromHour != "" && toHour != "") {
      val fromDate = getDate(fromHour)
      val toDate = getDate(toHour)
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

  fun getNextWeekdayTime(reminder: Reminder, fromTime: Long = System.currentTimeMillis()): Long {
    val weekdays = reminder.weekdays
    val beforeValue = reminder.remindBefore
    val calendar = (if (reminder.eventTime != "") {
      newCalendar(getDateTimeFromGmt(reminder.eventTime))
    } else droppedCalendar())
    while (true) {
      if (weekdays[calendar.getDayOfWeek() - 1] == 1 && calendar.timeInMillis - beforeValue > fromTime) {
        break
      }
      calendar.addMillis(DAY)
    }
    return calendar.timeInMillis
  }

  fun getNextWeekdayTime(startTime: Long, weekdays: List<Int>, delay: Long): Long {
    val calendar = newCalendar(startTime).also {
      it.dropSeconds()
      it.dropMilliseconds()
    }
    return if (delay > 0) {
      startTime + delay * MINUTE
    } else {
      while (true) {
        if (weekdays[calendar.getDayOfWeek() - 1] == 1 && calendar.timeInMillis > System.currentTimeMillis()) {
          break
        }
        calendar.timeInMillis = calendar.timeInMillis + DAY
      }
      calendar.timeInMillis
    }
  }

  private fun calendarFromEventTime(eventTime: String, fromTime: Long) =
    newCalendar().takeIf { eventTime != "" }?.apply {
      this.setMillis(getDateTimeFromGmt(eventTime))
      val time = Time(getHourOfDay(), getMinute(), getSecond())
      this.setMillis(fromTime)
      this.setTime(time.hour, time.minute)
      this.dropSeconds()
      this.dropMilliseconds()
    } ?: droppedCalendar()

  private fun droppedCalendar() = newCalendar().apply {
    this.dropSeconds()
    this.dropMilliseconds()
  }

  private fun dateTime24Formatter(): DateTimeFormatter = localizedDateFormatter("dd MMM yyyy, HH:mm")

  private fun dateTime12Formatter(): DateTimeFormatter = localizedDateFormatter("dd MMM yyyy, h:mm a")

  private fun dateTime24(): SimpleDateFormat = localizedDateFormat("dd MMM yyyy, HH:mm")

  private fun dateTime12(): SimpleDateFormat = localizedDateFormat("dd MMM yyyy, h:mm a")

  fun fullDate(): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy")

  fun fullDateFormatter(): DateTimeFormatter = localizedDateFormatter("EEE, dd MMM yyyy")

  private fun fullDateTime24(): SimpleDateFormat =
    localizedDateFormat("EEE, dd MMM yyyy HH:mm")

  private fun fullDateTime12(): SimpleDateFormat =
    localizedDateFormat("EEE, dd MMM yyyy h:mm a")

  private fun time24(): SimpleDateFormat = localizedDateFormat("HH:mm")

  private fun time24Formatter(): DateTimeFormatter = localizedDateFormatter("HH:mm")

  private fun time12(): SimpleDateFormat = localizedDateFormat("h:mm a")

  private fun time12Formatter(): DateTimeFormatter = localizedDateFormatter("h:mm a")

  fun simpleDate(): SimpleDateFormat = localizedDateFormat("d MMMM")

  fun date(): SimpleDateFormat = localizedDateFormat("dd MMM yyyy")

  fun dateFormatter(): DateTimeFormatter = localizedDateFormatter("dd MMM yyyy")

  fun day(): SimpleDateFormat = localizedDateFormat("dd")

  fun month(): SimpleDateFormat = localizedDateFormat("MMM")

  fun year(): SimpleDateFormat = localizedDateFormat("yyyy")

  companion object {
    const val SECOND: Long = 1000
    const val MINUTE: Long = 60 * SECOND
    const val HOUR: Long = MINUTE * 60
    const val HALF_DAY: Long = HOUR * 12
    const val DAY: Long = HALF_DAY * 2
    const val WEEK: Long = DAY * 7

    private const val GMT = "GMT"
    private val GMT_ZONE_ID = ZoneId.of(GMT)

    val BIRTH_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val BIRTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    val BIRTH_FORMAT = SimpleDateFormat("dd|MM", Locale.US)
    private val VOICE_ENGINE_GMT_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val GMT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
    private val GMT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
    private val FIRE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val FIRE_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
    private val TIME_24 = SimpleDateFormat("HH:mm", Locale.US)

    val gmtDateTime: String
      get() {
        GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
        return try {
          GMT_DATE_FORMAT.format(Date())
        } catch (e: Throwable) {
          ""
        }
      }

    fun generateViewAfterString(time: Long): String {
      val s: Long = 1000
      val m = s * 60
      val h = m * 60
      val hours = time / h
      val minutes = (time - hours * h) / m
      val seconds = (time - hours * h - minutes * m) / s
      val hourStr: String = if (hours < 10) {
        "0$hours"
      } else {
        hours.toString()
      }
      val minuteStr: String = if (minutes < 10) {
        "0$minutes"
      } else {
        minutes.toString()
      }
      val secondStr: String = if (seconds < 10) {
        "0$seconds"
      } else {
        seconds.toString()
      }
      return "$hourStr:$minuteStr:$secondStr"
    }
  }

  data class Date(val year: Int, val month: Int, val day: Int)
  data class Time(val hour: Int, val minute: Int, val second: Int)
  data class BirthDate(val millis: Long, val year: Int)
}
