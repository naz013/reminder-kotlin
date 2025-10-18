package com.github.naz013.common.datetime

import android.app.AlarmManager
import com.github.naz013.logging.Logger
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale

class DateTimeManager(
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val dateTimePreferences: DateTimePreferences
) {

  fun fromRfc3339ToLocal(date: String?): LocalDateTime? {
    if (date == null) return null
    return runCatching { ZonedDateTime.parse(date).toLocalDateTime() }.getOrNull()
  }

  fun getCurrentDateTime(): LocalDateTime {
    return nowDateTimeProvider.nowDateTime()
  }

  fun getCurrentDate(): LocalDate {
    return getCurrentDateTime().toLocalDate()
  }

  fun getPlaceDateTimeFromGmt(dateTime: String?): LocalDate? {
    return try {
      fromGmtToLocal(dateTime)?.toLocalDate()
    } catch (e: Throwable) {
      null
    }
  }

  fun formatBirthdayDateForUi(date: LocalDate, ignoreYear: Boolean): String {
    return if (ignoreYear) {
      formatBirthdayDateForUi(date)
    } else {
      formatBirthdayFullDateForUi(date)
    }
  }

  fun formatBirthdayFullDateForUi(date: LocalDate): String {
    return date.format(headerDateFormatter())
  }

  fun formatBirthdayDateForUi(date: LocalDate): String {
    return date.format(dayMonthBirthdayUiFormatter())
  }

  fun formatBirthdayDate(date: LocalDate): String {
    return date.format(BIRTH_DATE_FORMATTER)
  }

  fun parseBirthdayDate(date: String): LocalDate? {
    return try {
      LocalDate.parse(date, BIRTH_DATE_FORMATTER)
    } catch (e: Throwable) {
      Logger.e("parseBirthdayDate: failed = $date", e)
      null
    }
  }

  fun fromMillis(millis: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
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

  fun millisToEndDnd(from: String?, to: String?, current: LocalDateTime): Long {
    return doNotDisturbRange(from, to).last - toMillis(current)
  }

  fun doNotDisturbRange(from: String?, to: String?): LongRange {
    val fromTime = toLocalTime(from) ?: return LongRange(0, 0)
    val toTime = toLocalTime(to) ?: return LongRange(0, 0)

    Logger.d("doNotDisturbRange: HM $fromTime, $toTime")
    val compare = compareHm(fromTime, toTime)
    val fromMillis = toMillis(LocalDateTime.of(LocalDate.now(), fromTime))
    var toMillis = toMillis(LocalDateTime.of(LocalDate.now(), toTime))
    if (compare < 0) {
      if (toMillis < fromMillis) {
        toMillis += AlarmManager.INTERVAL_DAY
      }
    } else if (compare == 0) {
      return LongRange(0, 0)
    }
    Logger.d("doNotDisturbRange: millis $fromMillis, $toMillis")
    return if (fromMillis > toMillis) {
      LongRange(toMillis, fromMillis)
    } else {
      LongRange(fromMillis, toMillis)
    }
  }

  private fun compareHm(from: LocalTime, to: LocalTime): Int {
    return when {
      from.hour == to.hour -> when {
        from.minute == to.minute -> 0
        from.minute > to.minute -> -1
        else -> 1
      }

      from.hour > to.hour -> -1
      else -> 1
    }
  }

  fun fromGmtToLocal(dateTime: String?): LocalDateTime? {
    if (dateTime.isNullOrEmpty()) return null
    return try {
      gmtToLocal(dateTime, GMT_DATE_FORMATTER)
    } catch (e: Throwable) {
      null
    }
  }

  fun getNowGmtDateTime(): String {
    return try {
      getCurrentDateTime()
        .atZone(ZoneId.systemDefault())
        .format(GMT_DATE_FORMATTER.withZone(ZoneId.of(GMT)))
    } catch (e: Throwable) {
      ""
    }
  }

  fun getGmtFromDateTime(date: LocalDate): String {
    return try {
      LocalDateTime.of(date, nowDateTimeProvider.nowTime())
        .atZone(ZoneId.systemDefault())
        .format(GMT_DATE_FORMATTER.withZone(ZoneId.of(GMT)))
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
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

  fun toMillis(zonedDateTime: ZonedDateTime): Long {
    return zonedDateTime.toInstant().toEpochMilli()
  }

  fun toMillis(dateTime: String?): Long {
    return fromGmtToLocal(dateTime)?.let { toMillis(it) } ?: 0L
  }

  fun toGoogleTaskDate(localDate: LocalDate): String {
    return localDate.format(fullDateFormatter())
  }

  fun getDate(date: LocalDate): String {
    return date.format(dateFormatter())
  }

  fun logDateTime(dateTime: LocalDateTime = LocalDateTime.now()): String {
    return dateTime.format(fullDateTime24Formatter())
  }

  fun logDateTime(dateTime: String?): String {
    return fromGmtToLocal(dateTime)?.let { logDateTime(it) } ?: ""
  }

  fun getFullDateTime(millis: Long): String {
    return getFullDateTime(fromMillis(millis))
  }

  fun getFullDateTime(dateTime: String?): String {
    return fromGmtToLocal(dateTime)?.let { getFullDateTime(it) } ?: ""
  }

  fun getFullDateTime(dateTime: LocalDateTime): String {
    return if (dateTimePreferences.is24HourFormat) {
      dateTime.format(fullDateTime24Formatter())
    } else {
      dateTime.format(fullDateTime12Formatter())
    }
  }

  fun isSameDay(birthDate: LocalDate, current: LocalDate = getCurrentDate()): Boolean {
    return birthDate.dayOfMonth == current.dayOfMonth && birthDate.monthValue == current.monthValue
  }

  fun getReadableBirthDate(dateOfBirth: LocalDate?, ignoreYear: Boolean): String {
    if (dateOfBirth == null) return ""
    val formatter = if (ignoreYear) {
      dayMonthBirthdayUiFormatter()
    } else {
      headerDateFormatter()
    }
    return try {
      dateOfBirth.format(formatter)
    } catch (e: Throwable) {
      ""
    }
  }

  fun getMillisToBirthdayTime(): Long {
    val birthdayTime = getBirthdayLocalTime() ?: return 0L
    var dateTime = LocalDateTime.of(nowDateTimeProvider.nowDate(), birthdayTime)
    if (dateTime.isBefore(getCurrentDateTime())) {
      dateTime = dateTime.plusDays(1)
    }
    return ChronoUnit.MILLIS.between(getCurrentDateTime(), dateTime)
  }

  fun getBirthdayLocalTime(): LocalTime? {
    var time = toLocalTime(dateTimePreferences.birthdayTime) ?: return null
    if (time.isBefore(nowDateTimeProvider.nowTime())) {
      time = time.plusHours(24)
    }
    return time
  }

  fun getBirthdayVisualTime(): String {
    return getBirthdayLocalTime()?.let { getTime(it) } ?: ""
  }

  fun getDayStart(dateTime: LocalDateTime = getCurrentDateTime()): String {
    return dateTime.withHour(0)
      .withMinute(0)
      .withSecond(0)
      .let { getGmtFromDateTime(it) }
  }

  fun getDayEnd(dateTime: LocalDateTime = getCurrentDateTime()): String {
    return getDayStart(dateTime.plusDays(1))
  }

  fun getBirthdayDayMonth(
    dateTime: LocalDateTime = getCurrentDateTime()
  ): String {
    return "${dateTime.dayOfMonth}|${dateTime.monthValue - 1}"
  }

  fun toLocalTime(time24: String?): LocalTime? {
    return try {
      LocalTime.parse(time24, TIME_24_FORMATTER)
    } catch (e: Throwable) {
      try {
        LocalTime.parse(time24, TIME_24_FORMATTER_SHORT)
      } catch (t: Throwable) {
        null
      }
    }
  }

  fun to24HourString(time: LocalTime): String {
    return time.format(TIME_24_FORMATTER)
  }

  private fun localizedDateFormatter(pattern: String): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(
      pattern,
      dateTimePreferences.locale
    )
  }

  fun getTime(time: LocalTime): String {
    return if (dateTimePreferences.is24HourFormat) {
      time.format(time24Formatter())
    } else {
      time.format(time12Formatter())
    }
  }

  fun isCurrent(eventTime: String?): Boolean {
    return fromGmtToLocal(eventTime)?.let {
      getCurrentDateTime().isBefore(it)
    } ?: false
  }

  fun isCurrent(dateTime: LocalDateTime): Boolean {
    return dateTime.isAfter(getCurrentDateTime())
  }

  fun getDateTime(dateTime: LocalDateTime): String {
    return if (dateTimePreferences.is24HourFormat) {
      dateTime.format(dateTime24Formatter())
    } else {
      dateTime.format(dateTime12Formatter())
    }
  }

  fun generateDateTime(
    eventTime: String,
    repeat: Long,
    fromTime: LocalDateTime = getCurrentDateTime()
  ): LocalDateTime {
    var time = fromGmtToLocal(eventTime) ?: return LocalDateTime.now()
    while (time <= fromTime) {
      time = time.plusMillis(repeat)
    }
    return time
  }

  fun localDayOfWeekToOld(dayOfWeek: DayOfWeek): Int {
    // monday = 1 - sunday = 7
    return if (dayOfWeek == DayOfWeek.SUNDAY) {
      1
    } else {
      dayOfWeek.value + 1
    }
  }

  fun isAfterDate(gmt1: String?, gmt2: String?): Boolean {
    if (gmt1.isNullOrEmpty()) return false
    if (gmt2.isNullOrEmpty()) return true
    val dateTime1 = fromGmtToLocal(gmt1) ?: return false
    val dateTime2 = fromGmtToLocal(gmt2) ?: return true
    return dateTime1 > dateTime2
  }

  fun getBirthdayDateSearch(date: LocalDate): String {
    return date.format(birthdaySearchDayMonth())
  }

  fun formatCalendarDate(date: LocalDate): String {
    return date.format(calendarFullDate())
  }

  fun formatCalendarMonthYear(date: LocalDate): String {
    return date.format(calendarMonthYear())
  }

  fun formatCalendarWeekday(date: LocalDate): String {
    return date.format(shortWeekDay())
  }

  fun formatCalendarDay(date: LocalDate): String {
    return date.format(shortDay())
  }

  fun findBirthdayDate(birthdayDate: String): LocalDate? {
    var date: LocalDate? = null
    for (formatter in birthdayFormats) {
      date = runCatching { LocalDate.parse(birthdayDate, formatter) }.getOrNull()
      if (date != null) {
        break
      }
    }
    return date
  }

  fun getHeaderDateFormatted(date: LocalDate): String {
    return date.format(headerDateFormatter())
  }

  fun parseBeforeTime(millis: Long): BeforeTime {
    return Companion.parseBeforeTime(millis)
  }

  fun parseRepeatTime(millis: Long): RepeatTime {
    return Companion.parseRepeatTime(millis)
  }

  fun formatMonth(date: LocalDate): String {
    return date.format(monthFormatter())
  }

  fun formatDayMonth(date: LocalDate): String {
    return date.format(dayMonthFormatter())
  }

  private fun dateTime24Formatter(): DateTimeFormatter =
    localizedDateFormatter("dd MMM yyyy, HH:mm")

  private fun dateTime12Formatter(): DateTimeFormatter =
    localizedDateFormatter("dd MMM yyyy, h:mm a")

  fun fullDateFormatter(): DateTimeFormatter = localizedDateFormatter("EEE, dd MMM yyyy")

  private fun fullDateTime24Formatter(): DateTimeFormatter =
    localizedDateFormatter("EEE, dd MMM yyyy HH:mm")

  private fun fullDateTime12Formatter(): DateTimeFormatter =
    localizedDateFormatter("EEE, dd MMM yyyy h:mm a")

  private fun time24Formatter(): DateTimeFormatter = localizedDateFormatter("HH:mm")

  private fun time12Formatter(): DateTimeFormatter = localizedDateFormatter("h:mm a")

  private fun dayMonthFormatter(): DateTimeFormatter = localizedDateFormatter("dd MMMM")

  private fun headerDateFormatter(): DateTimeFormatter = localizedDateFormatter("d MMMM yyyy")

  private fun dayMonthBirthdayUiFormatter(): DateTimeFormatter =
    localizedDateFormatter("d MMMM")

  private fun dateFormatter(): DateTimeFormatter = localizedDateFormatter("dd MMM yyyy")

  private fun birthdaySearchDayMonth(): DateTimeFormatter = localizedDateFormatter("dd|MM")

  private fun calendarFullDate(): DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

  private fun calendarMonthYear(): DateTimeFormatter = localizedDateFormatter("MMMM yyyy")

  private fun shortWeekDay(): DateTimeFormatter = localizedDateFormatter("EEE")

  private fun shortDay(): DateTimeFormatter = localizedDateFormatter("d")

  private fun monthFormatter(): DateTimeFormatter = localizedDateFormatter("MMMM")

  companion object {
    const val SECOND: Long = 1000
    const val MINUTE: Long = 60 * SECOND
    const val HOUR: Long = MINUTE * 60
    private const val HALF_DAY: Long = HOUR * 12
    const val DAY: Long = HALF_DAY * 2
    const val WEEK: Long = DAY * 7

    private const val GMT = "GMT"

    private val BIRTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val GMT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
    private val TIME_24_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val TIME_24_FORMATTER_SHORT = DateTimeFormatter.ofPattern("H[H]:m[m]", Locale.US)

    private val birthdayFormats: List<DateTimeFormatter> = listOf(
      DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US),
      DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US),
      DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.US),
      DateTimeFormatter.ofPattern("yy.MM.dd", Locale.US),
      DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US),
      DateTimeFormatter.ofPattern("yy/MM/dd", Locale.US)
    )

    val gmtDateTime: String
      get() {
        return try {
          LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .format(GMT_DATE_FORMATTER.withZone(ZoneId.of(GMT)))
        } catch (e: Throwable) {
          ""
        }
      }

    fun generateViewAfterString(time: Long, divider: String = ":"): String {
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
      return "$hourStr$divider$minuteStr$divider$secondStr"
    }

    fun parseBeforeTime(millis: Long): BeforeTime {
      if (millis == 0L) {
        return BeforeTime(0, MultiplierType.SECOND)
      }
      return when {
        millis % (DAY * 30) == 0L -> {
          val progress = millis / (DAY * 30)
          BeforeTime(progress, MultiplierType.MONTH)
        }

        millis % (DAY * 7) == 0L -> {
          val progress = millis / (DAY * 7)
          BeforeTime(progress, MultiplierType.WEEK)
        }

        millis % DAY == 0L -> {
          val progress = millis / DAY
          BeforeTime(progress, MultiplierType.DAY)
        }

        millis % HOUR == 0L -> {
          val progress = millis / HOUR
          BeforeTime(progress, MultiplierType.HOUR)
        }

        millis % MINUTE == 0L -> {
          val progress = millis / MINUTE
          BeforeTime(progress, MultiplierType.MINUTE)
        }

        millis % SECOND == 0L -> {
          val progress = millis / SECOND
          BeforeTime(progress, MultiplierType.SECOND)
        }

        else -> {
          BeforeTime(0, MultiplierType.SECOND)
        }
      }
    }

    fun parseRepeatTime(millis: Long): RepeatTime {
      if (millis == 0L) {
        return RepeatTime(0, MultiplierType.SECOND)
      }
      return when {
        millis % (DAY * 30) == 0L -> {
          val progress = millis / (DAY * 30)
          RepeatTime(progress, MultiplierType.MONTH)
        }

        millis % (DAY * 7) == 0L -> {
          val progress = millis / (DAY * 7)
          RepeatTime(progress, MultiplierType.WEEK)
        }

        millis % DAY == 0L -> {
          val progress = millis / DAY
          RepeatTime(progress, MultiplierType.DAY)
        }

        millis % HOUR == 0L -> {
          val progress = millis / HOUR
          RepeatTime(progress, MultiplierType.HOUR)
        }

        millis % MINUTE == 0L -> {
          val progress = millis / MINUTE
          RepeatTime(progress, MultiplierType.MINUTE)
        }

        millis % SECOND == 0L -> {
          val progress = millis / SECOND
          RepeatTime(progress, MultiplierType.SECOND)
        }

        else -> {
          RepeatTime(0, MultiplierType.SECOND)
        }
      }
    }
  }

  data class Date(val year: Int, val month: Int, val day: Int)
  data class Time(val hour: Int, val minute: Int, val second: Int)
  data class BeforeTime(val value: Long, val type: MultiplierType)
  data class RepeatTime(val value: Long, val type: MultiplierType)
  enum class MultiplierType(val index: Int) {
    SECOND(0),
    MINUTE(1),
    HOUR(2),
    DAY(3),
    WEEK(4),
    MONTH(5)
  }
}
