package com.github.naz013.datecalc

import com.github.naz013.logging.Logger
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale

class DateTimeManager(
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val dateTimePreferences: DateTimePreferences,
) {
  fun fromRfc3339ToLocal(date: String?): LocalDateTime? {
    if (date == null) return null
    return runCatching { ZonedDateTime.parse(date).toLocalDateTime() }.getOrNull()
  }

  fun getCurrentDateTime(): LocalDateTime = nowDateTimeProvider.nowDateTime()

  fun getCurrentDate(): LocalDate = getCurrentDateTime().toLocalDate()

  fun getPlaceDateTimeFromGmt(dateTime: String?): LocalDate? =
    try {
      fromGmtToLocal(dateTime)?.toLocalDate()
    } catch (_: Throwable) {
      null
    }

  fun formatBirthdayDateForUi(
    date: LocalDate,
    ignoreYear: Boolean,
  ): String =
    if (ignoreYear) {
      formatBirthdayDateForUi(date)
    } else {
      formatBirthdayFullDateForUi(date)
    }

  fun formatBirthdayFullDateForUi(date: LocalDate): String = date.format(headerDateFormatter())

  fun formatBirthdayDateForUi(date: LocalDate): String = date.format(dayMonthBirthdayUiFormatter())

  fun formatBirthdayDate(date: LocalDate): String = date.format(BIRTH_DATE_FORMATTER)

  fun parseBirthdayDate(date: String): LocalDate? =
    try {
      LocalDate.parse(date, BIRTH_DATE_FORMATTER)
    } catch (e: Throwable) {
      Logger.e(TAG, "parseBirthdayDate: failed = $date", e)
      null
    }

  fun fromMillis(millis: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

  /** `ReminderV2`'s schedule fields are stored as UTC wall-clock [org.threeten.bp.LocalDateTime]s (the same real
   * instant as V1's GMT string, just expressed in the UTC zone instead of parsed on demand) -
   * convert to this device's local wall-clock before displaying or comparing against [getCurrentDateTime]. */
  fun utcToLocal(dateTime: LocalDateTime): LocalDateTime =
    dateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()

  /** Inverse of [utcToLocal] - converts a local wall-clock [org.threeten.bp.LocalDateTime] into the UTC wall-clock
   * form `ReminderV2`'s schedule fields (and range queries against them) expect. */
  fun localToUtc(dateTime: LocalDateTime): LocalDateTime =
    dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

  private fun gmtToLocal(
    gmt: String?,
    formatter: DateTimeFormatter,
  ): LocalDateTime? =
    if (gmt == null) {
      null
    } else {
      ZonedDateTime
        .parse(gmt, formatter)
        .withZoneSameInstant(ZoneId.systemDefault())
        .toLocalDateTime()
    }

  fun millisToEndDnd(
    from: String?,
    to: String?,
    current: LocalDateTime,
  ): Long = doNotDisturbRange(from, to).last - toMillis(current)

  fun doNotDisturbRange(
    from: String?,
    to: String?,
  ): LongRange {
    val fromTime = toLocalTime(from) ?: return LongRange(0, 0)
    val toTime = toLocalTime(to) ?: return LongRange(0, 0)

    Logger.d(TAG, "doNotDisturbRange: HM $fromTime, $toTime")
    val compare = compareHm(fromTime, toTime)
    val fromMillis = toMillis(LocalDateTime.of(LocalDate.now(), fromTime))
    var toMillis = toMillis(LocalDateTime.of(LocalDate.now(), toTime))
    if (compare < 0) {
      if (toMillis < fromMillis) {
        toMillis += DAY
      }
    } else if (compare == 0) {
      return LongRange(0, 0)
    }
    Logger.d(TAG, "doNotDisturbRange: millis $fromMillis, $toMillis")
    return if (fromMillis > toMillis) {
      LongRange(toMillis, fromMillis)
    } else {
      LongRange(fromMillis, toMillis)
    }
  }

  private fun compareHm(
    from: LocalTime,
    to: LocalTime,
  ): Int =
    when {
      from.hour == to.hour ->
        when {
          from.minute == to.minute -> 0
          from.minute > to.minute -> -1
          else -> 1
        }

      from.hour > to.hour -> -1
      else -> 1
    }

  fun fromGmtToLocal(dateTime: String?): LocalDateTime? {
    if (dateTime.isNullOrEmpty()) return null
    return try {
      gmtToLocal(dateTime, GMT_DATE_FORMATTER)
    } catch (_: Throwable) {
      null
    }
  }

  fun getNowGmtDateTime(): String =
    try {
      getCurrentDateTime()
        .atZone(ZoneId.systemDefault())
        .format(GMT_DATE_FORMATTER.withZone(ZoneId.of(GMT)))
    } catch (_: Throwable) {
      ""
    }

  fun getGmtFromDateTime(date: LocalDate): String =
    try {
      LocalDateTime
        .of(date, nowDateTimeProvider.nowTime())
        .atZone(ZoneId.systemDefault())
        .format(GMT_DATE_FORMATTER.withZone(ZoneId.of(GMT)))
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
    }

  fun getGmtFromDateTime(dateTime: LocalDateTime): String =
    try {
      dateTime.atZone(ZoneId.systemDefault()).format(GMT_DATE_FORMATTER.withZone(ZoneId.of(GMT)))
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
    }

  fun toMillis(localDateTime: LocalDateTime): Long = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli()

  fun toMillis(zonedDateTime: ZonedDateTime): Long = zonedDateTime.toInstant().toEpochMilli()

  fun toMillis(dateTime: String?): Long = fromGmtToLocal(dateTime)?.let { toMillis(it) } ?: 0L

  fun toGoogleTaskDate(localDate: LocalDate): String = localDate.format(fullDateFormatter())

  fun getDate(date: LocalDate): String = date.format(dateFormatter())

  fun logDateTime(dateTime: LocalDateTime = LocalDateTime.now()): String = dateTime.format(fullDateTime24Formatter())

  fun getFullDateTime(millis: Long): String = getFullDateTime(fromMillis(millis))

  fun getFullDateTime(dateTime: String?): String = fromGmtToLocal(dateTime)?.let { getFullDateTime(it) } ?: ""

  fun getFullDateTime(dateTime: LocalDateTime): String =
    if (dateTimePreferences.is24HourFormat) {
      dateTime.format(fullDateTime24Formatter())
    } else {
      dateTime.format(fullDateTime12Formatter())
    }

  fun isSameDay(
    birthDate: LocalDate,
    current: LocalDate = getCurrentDate(),
  ): Boolean = birthDate.dayOfMonth == current.dayOfMonth && birthDate.monthValue == current.monthValue

  fun getReadableBirthDate(
    dateOfBirth: LocalDate?,
    ignoreYear: Boolean,
  ): String {
    if (dateOfBirth == null) return ""
    val formatter =
      if (ignoreYear) {
        dayMonthBirthdayUiFormatter()
      } else {
        headerDateFormatter()
      }
    return try {
      dateOfBirth.format(formatter)
    } catch (_: Throwable) {
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

  fun getBirthdayVisualTime(): String = getBirthdayLocalTime()?.let { getTime(it) } ?: ""

  fun getDayStart(dateTime: LocalDateTime = getCurrentDateTime()): String =
    dateTime
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .let { getGmtFromDateTime(it) }

  fun getDayEnd(dateTime: LocalDateTime = getCurrentDateTime()): String = getDayStart(dateTime.plusDays(1))

  fun getBirthdayDayMonth(dateTime: LocalDateTime = getCurrentDateTime()): String = "${dateTime.dayOfMonth}|${dateTime.monthValue - 1}"

  fun toLocalTime(time24: String?): LocalTime? =
    try {
      LocalTime.parse(time24, TIME_24_FORMATTER)
    } catch (_: Throwable) {
      try {
        LocalTime.parse(time24, TIME_24_FORMATTER_SHORT)
      } catch (_: Throwable) {
        null
      }
    }

  fun to24HourString(time: LocalTime): String = time.format(TIME_24_FORMATTER)

  private fun localizedDateFormatter(pattern: String): DateTimeFormatter =
    DateTimeFormatter.ofPattern(
      pattern,
      dateTimePreferences.locale,
    )

  fun getTime(time: LocalTime): String =
    if (dateTimePreferences.is24HourFormat) {
      time.format(time24Formatter())
    } else {
      time.format(time12Formatter())
    }

  fun isCurrent(eventTime: String?): Boolean =
    fromGmtToLocal(eventTime)?.let {
      getCurrentDateTime().isBefore(it)
    } ?: false

  fun isCurrent(dateTime: LocalDateTime): Boolean = dateTime.isAfter(getCurrentDateTime())

  fun getDateTime(dateTime: LocalDateTime): String =
    if (dateTimePreferences.is24HourFormat) {
      dateTime.format(dateTime24Formatter())
    } else {
      dateTime.format(dateTime12Formatter())
    }

  fun localDayOfWeekToOld(dayOfWeek: DayOfWeek): Int {
    // monday = 1 - sunday = 7
    return if (dayOfWeek == DayOfWeek.SUNDAY) {
      1
    } else {
      dayOfWeek.value + 1
    }
  }

  fun getBirthdayDateSearch(date: LocalDate): String = date.format(birthdaySearchDayMonth())

  fun formatCalendarDate(date: LocalDate): String = date.format(calendarFullDate())

  fun formatCalendarMonthYear(date: LocalDate): String = date.format(calendarMonthYear())

  fun formatCalendarWeekday(date: LocalDate): String = date.format(shortWeekDay())

  fun formatCalendarDay(date: LocalDate): String = date.format(shortDay())

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

  fun getHeaderDateFormatted(date: LocalDate): String = date.format(headerDateFormatter())

  fun parseBeforeTime(millis: Long): BeforeTime = Companion.parseBeforeTime(millis)

  fun parseRepeatTime(millis: Long): RepeatTime = Companion.parseRepeatTime(millis)

  fun formatMonth(date: LocalDate): String = date.format(monthFormatter())

  fun formatDayMonth(date: LocalDate): String = date.format(dayMonthFormatter())

  private fun dateTime24Formatter(): DateTimeFormatter = localizedDateFormatter("dd MMM yyyy, HH:mm")

  private fun dateTime12Formatter(): DateTimeFormatter = localizedDateFormatter("dd MMM yyyy, h:mm a")

  fun fullDateFormatter(): DateTimeFormatter = localizedDateFormatter("EEE, dd MMM yyyy")

  private fun fullDateTime24Formatter(): DateTimeFormatter = localizedDateFormatter("EEE, dd MMM yyyy HH:mm")

  private fun fullDateTime12Formatter(): DateTimeFormatter = localizedDateFormatter("EEE, dd MMM yyyy h:mm a")

  private fun time24Formatter(): DateTimeFormatter = localizedDateFormatter("HH:mm")

  private fun time12Formatter(): DateTimeFormatter = localizedDateFormatter("h:mm a")

  private fun dayMonthFormatter(): DateTimeFormatter = localizedDateFormatter("dd MMMM")

  private fun headerDateFormatter(): DateTimeFormatter = localizedDateFormatter("d MMMM yyyy")

  private fun dayMonthBirthdayUiFormatter(): DateTimeFormatter = localizedDateFormatter("d MMMM")

  private fun dateFormatter(): DateTimeFormatter = localizedDateFormatter("dd MMM yyyy")

  private fun birthdaySearchDayMonth(): DateTimeFormatter = localizedDateFormatter("dd|MM")

  private fun calendarFullDate(): DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

  private fun calendarMonthYear(): DateTimeFormatter = localizedDateFormatter("MMMM yyyy")

  private fun shortWeekDay(): DateTimeFormatter = localizedDateFormatter("EEE")

  private fun shortDay(): DateTimeFormatter = localizedDateFormatter("d")

  private fun monthFormatter(): DateTimeFormatter = localizedDateFormatter("MMMM")

  companion object {
    private const val TAG = "DateTimeManager"

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

    private val birthdayFormats: List<DateTimeFormatter> =
      listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US),
        DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US),
        DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.US),
        DateTimeFormatter.ofPattern("yy.MM.dd", Locale.US),
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US),
        DateTimeFormatter.ofPattern("yy/MM/dd", Locale.US),
      )

    val gmtDateTime: String
      get() {
        return try {
          LocalDateTime
            .now()
            .atZone(ZoneId.systemDefault())
            .format(GMT_DATE_FORMATTER.withZone(ZoneId.of(GMT)))
        } catch (_: Throwable) {
          ""
        }
      }

    fun generateViewAfterString(
      time: Long,
      divider: String = ":",
    ): String {
      val s: Long = 1000
      val m = s * 60
      val h = m * 60
      val hours = time / h
      val minutes = (time - hours * h) / m
      val seconds = (time - hours * h - minutes * m) / s
      val hourStr: String =
        if (hours < 10) {
          "0$hours"
        } else {
          hours.toString()
        }
      val minuteStr: String =
        if (minutes < 10) {
          "0$minutes"
        } else {
          minutes.toString()
        }
      val secondStr: String =
        if (seconds < 10) {
          "0$seconds"
        } else {
          seconds.toString()
        }
      return "$hourStr$divider$minuteStr$divider$secondStr"
    }

    fun parseBeforeTime(millis: Long): BeforeTime {
      val duration = DurationDecomposer.decompose(millis)
      return BeforeTime(duration.value, MultiplierType.valueOf(duration.unit.name))
    }

    fun parseRepeatTime(millis: Long): RepeatTime {
      val duration = DurationDecomposer.decompose(millis)
      return RepeatTime(duration.value, MultiplierType.valueOf(duration.unit.name))
    }
  }

  data class Date(
    val year: Int,
    val month: Int,
    val day: Int,
  )

  data class Time(
    val hour: Int,
    val minute: Int,
    val second: Int,
  )

  data class BeforeTime(
    val value: Long,
    val type: MultiplierType,
  )

  data class RepeatTime(
    val value: Long,
    val type: MultiplierType,
  )

  enum class MultiplierType(
    val index: Int,
  ) {
    SECOND(0),
    MINUTE(1),
    HOUR(2),
    DAY(3),
    WEEK(4),
    MONTH(5),
  }
}
