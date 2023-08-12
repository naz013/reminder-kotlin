package com.elementary.tasks.core.utils.datetime

import android.app.AlarmManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.minusMillis
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.plusMillis
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.util.Locale

class DateTimeManager(
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val language: Language,
  private val nowDateTimeProvider: NowDateTimeProvider
) {

  fun toRfc3339Format(millis: Long): String {
    return ZonedDateTime.of(
      fromMillis(millis),
      ZoneId.systemDefault()
    ).format(RFC3339_DATE_FORMATTER).also {
      Timber.d("toRfc3339Format: $it")
    }
  }

  fun fromRfc3339Format(date: String?): Long {
    if (date == null) return 0L
    val dateTime = ZonedDateTime.parse(date)
    return toMillis(dateTime.toLocalDateTime())
  }

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
      Timber.d(e, "parseBirthdayDate: failed = $date")
      null
    }
  }

  fun fromMillis(millis: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
  }

  fun isAfterNow(gmt: String?): Boolean {
    return try {
      gmtToLocal(gmt, DateTimeFormatter.ofPattern(FIRE_DATE_PATTERN, Locale.US))
        ?.isAfter(getCurrentDateTime()) ?: false
    } catch (e: Throwable) {
      false
    }
  }

  fun getFireFormatted(gmt: String?): String? {
    return gmtToLocal(gmt, DateTimeFormatter.ofPattern(FIRE_DATE_PATTERN, Locale.US))
      ?.let { getDateTime(it) }
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

    Timber.d("doNotDisturbRange: HM $fromTime, $toTime")
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
    Timber.d("doNotDisturbRange: millis $fromMillis, $toMillis")
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

  fun getFromGmtVoiceEngine(dateTime: String?): LocalDateTime? {
    if (dateTime.isNullOrEmpty()) return null
    return try {
      ZonedDateTime.parse(
        dateTime,
        VOICE_ENGINE_GMT_DATE_FORMAT.withZone(GMT_ZONE_ID)
      ).toLocalDateTime()
    } catch (e: Exception) {
      e.printStackTrace()
      null
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

  fun getNextDateTime(dateTime: LocalDateTime?): Array<String> {
    return if (dateTime == null) {
      arrayOf("", "")
    } else {
      arrayOf(
        dateTime.toLocalDate().format(dateFormatter()),
        getTime(dateTime.toLocalTime())
      )
    }
  }

  fun getRemaining(dateTime: String?, delay: Int): String {
    if (dateTime.isNullOrEmpty()) {
      return getRemaining(null)
    }
    return getRemaining(fromGmtToLocal(dateTime)?.plusMinutes(delay.toLong()))
  }

  fun getBirthdayRemaining(eventTime: LocalDateTime?, birthDate: LocalDate): String {
    return if (nowDateTimeProvider.nowDate().isBefore(birthDate)) {
      textProvider.getText(R.string.not_born)
    } else {
      getRemaining(eventTime)
    }
  }

  fun getRemaining(eventTime: LocalDateTime?): String {
    if (eventTime == null) return textProvider.getText(R.string.overdue)

    val nowDateTime = getCurrentDateTime()
    val days = ChronoUnit.DAYS.between(nowDateTime, eventTime)
    val hours = ChronoUnit.HOURS.between(nowDateTime, eventTime)
    val minutes = ChronoUnit.MINUTES.between(nowDateTime, eventTime)
    val seconds = ChronoUnit.SECONDS.between(nowDateTime, eventTime)

    val language = Language.getScreenLanguage(prefs.appLanguage).toString().lowercase()

    return if (days > 0) {
      if (language.startsWith("uk")) {
        var last = days
        while (last > 10) {
          last -= 10
        }
        if (last == 1L && days != 11L) {
          textProvider.getText(R.string.x_day, days.toString())
        } else if (last < 5 && (days < 12 || days > 14)) {
          textProvider.getText(R.string.x_dayzz, days.toString())
        } else {
          textProvider.getText(R.string.x_days, days.toString())
        }
      } else {
        if (days < 2) {
          textProvider.getText(R.string.x_day, days.toString())
        } else {
          textProvider.getText(R.string.x_days, days.toString())
        }
      }
    } else if (hours > 0) {
      if (language.startsWith("uk")) {
        var last = hours
        while (last > 10) {
          last -= 10
        }
        if (last == 1L && hours != 11L) {
          textProvider.getText(R.string.x_hour, hours.toString())
        } else if (last < 5 && (hours < 12 || hours > 14)) {
          textProvider.getText(R.string.x_hourzz, hours.toString())
        } else {
          textProvider.getText(R.string.x_hours, hours.toString())
        }
      } else {
        if (hours < 2) {
          textProvider.getText(R.string.x_hour, hours.toString())
        } else {
          textProvider.getText(R.string.x_hours, hours.toString())
        }
      }
    } else if (minutes > 0) {
      if (language.startsWith("uk")) {
        var last = minutes
        while (last > 10) {
          last -= 10
        }
        if (last == 1L && minutes != 11L) {
          textProvider.getText(R.string.x_minute, minutes.toString())
        } else if (last < 5 && (minutes < 12 || minutes > 14)) {
          textProvider.getText(R.string.x_minutezz, minutes.toString())
        } else {
          textProvider.getText(R.string.x_minutes, minutes.toString())
        }
      } else {
        if (minutes < 2) {
          textProvider.getText(R.string.x_minute, minutes.toString())
        } else {
          textProvider.getText(R.string.x_minutes, minutes.toString())
        }
      }
    } else if (seconds > 0) {
      textProvider.getText(R.string.less_than_minute)
    } else {
      textProvider.getText(R.string.overdue)
    }
  }

  fun getFullDateTime(millis: Long): String {
    return getFullDateTime(fromMillis(millis))
  }

  fun getFullDateTime(dateTime: String?): String {
    return fromGmtToLocal(dateTime)?.let { getFullDateTime(it) } ?: ""
  }

  fun getFullDateTime(dateTime: LocalDateTime): String {
    return if (prefs.is24HourFormat) {
      dateTime.format(fullDateTime24Formatter())
    } else {
      dateTime.format(fullDateTime12Formatter())
    }
  }

  fun isSameDay(birthDate: LocalDate, current: LocalDate = getCurrentDate()): Boolean {
    return birthDate.dayOfMonth == current.dayOfMonth && birthDate.monthValue == current.monthValue
  }

  fun getFutureBirthdayDate(birthdayTime: LocalTime, fullDate: String): BirthDate {
    return (parseBirthdayDate(fullDate) ?: nowDateTimeProvider.nowDate()).let { date ->
      var dateTime = LocalDateTime.of(nowDateTimeProvider.nowDate(), birthdayTime)
        .withMonth(date.monthValue)
        .withDayOfMonth(date.dayOfMonth)
      if (dateTime.isBefore(getCurrentDateTime())) {
        dateTime = dateTime.plusYears(1)
      }
      BirthDate(dateTime, date.year)
    }
  }

  fun getReadableBirthDate(dateOfBirth: String?, ignoreYear: Boolean): String {
    if (dateOfBirth.isNullOrEmpty()) return ""
    val formatter = if (ignoreYear) {
      dayMonthBirthdayUiFormatter()
    } else {
      headerDateFormatter()
    }
    return try {
      parseBirthdayDate(dateOfBirth)?.format(formatter) ?: dateOfBirth
    } catch (e: Throwable) {
      dateOfBirth
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
    var time = toLocalTime(prefs.birthdayTime) ?: return null
    if (time.isBefore(nowDateTimeProvider.nowTime())) {
      time = time.plusHours(24)
    }
    return time
  }

  fun getBirthdayVisualTime(): String {
    return getBirthdayLocalTime()?.let { getTime(it) } ?: ""
  }

  fun getAgeFormatted(date: String?): String {
    val years = getAge(date)
    val language = Language.getScreenLanguage(prefs.appLanguage).language.lowercase()
    return buildYearString(language, years)
  }

  fun getAgeFormatted(years: Int): String {
    val language = Language.getScreenLanguage(prefs.appLanguage).toString().lowercase()
    return buildYearString(language, years)
  }

  private fun buildYearString(language: String, years: Int): String {
    return if (language.startsWith("uk")) {
      var last = years.toLong()
      while (last > 10) {
        last -= 10
      }
      if (last == 1L && years != 11) {
        textProvider.getText(R.string.x_year, years.toString())
      } else if (last < 5 && (years < 12 || years > 14)) {
        textProvider.getText(R.string.x_yearzz, years.toString())
      } else {
        textProvider.getText(R.string.x_years, years.toString())
      }
    } else {
      if (years < 2) {
        textProvider.getText(R.string.x_year, years.toString())
      } else {
        textProvider.getText(R.string.x_years, years.toString())
      }
    }
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

  fun getBirthdayDayMonthList(
    start: LocalDateTime = getCurrentDateTime(),
    duration: Int = 1
  ): List<String> {
    val list = mutableListOf<String>()
    var dateTime: LocalDateTime
    for (n in 0 until duration) {
      dateTime = start.plusDays(n.toLong())
      list.add("${dateTime.dayOfMonth}|${dateTime.monthValue - 1}")
    }
    Timber.d("getBirthdayDayMonthList: $list")
    return list
  }

  fun getGmtDateTimeFromMillis(millis: Long): String {
    return getGmtFromDateTime(fromMillis(millis))
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

  fun getAge(dateOfBirth: String?): Int {
    if (dateOfBirth.isNullOrEmpty()) return 0
    val birthDate = parseBirthdayDate(dateOfBirth) ?: return 0
    return nowDateTimeProvider.nowDate().year - birthDate.year
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

  private fun isAllChecked(repCode: List<Int>): Boolean {
    return repCode.none { it == 0 }
  }

  private fun localizedDateFormatter(pattern: String): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(pattern, Language.getScreenLanguage(prefs.appLanguage))
  }

  fun getTime(time: LocalTime): String {
    return if (prefs.is24HourFormat) {
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
    return if (prefs.is24HourFormat) {
      dateTime.format(dateTime24Formatter())
    } else {
      dateTime.format(dateTime12Formatter())
    }
  }

  fun getNewNextMonthDayTime(
    reminder: Reminder,
    fromTime: LocalDateTime = getCurrentDateTime()
  ): LocalDateTime {
    val dayOfMonth = reminder.dayOfMonth
    val beforeValue = reminder.remindBefore

    Timber.d("getNextMonthDayTime: $dayOfMonth, before -> $beforeValue")

    if (dayOfMonth == 0) {
      return getLastMonthDayTime(fromTime, reminder)
    } else if (dayOfMonth > 28) {
      return getSmartMonthDayTime(fromTime, reminder)
    }

    val startDateTime = fromGmtToLocal(reminder.eventTime) ?: getCurrentDateTime()
    var dateTime = LocalDateTime.of(startDateTime.toLocalDate(), fromTime.toLocalTime())
      .withDayOfMonth(dayOfMonth)

    val interval = if (reminder.repeatInterval <= 0L) {
      1L
    } else {
      reminder.repeatInterval
    }

    var isAfter = dateTime.minusMillis(beforeValue).isAfter(fromTime)
    if (dateTime.dayOfMonth == dayOfMonth && isAfter) {
      return dateTime.withSecond(0)
    }
    while (true) {
      isAfter = dateTime.minusMillis(beforeValue).isAfter(fromTime)
      if (dateTime.dayOfMonth == dayOfMonth && isAfter) {
        break
      }
      dateTime = dateTime.plusMonths(interval)
    }
    return dateTime.withSecond(0)
  }

  private fun getSmartMonthDayTime(fromTime: LocalDateTime, reminder: Reminder): LocalDateTime {
    val dayOfMonth = reminder.dayOfMonth
    val beforeValue = reminder.remindBefore

    val startDateTime = fromGmtToLocal(reminder.eventTime) ?: getCurrentDateTime()
    var dateTime = LocalDateTime.of(startDateTime.toLocalDate(), fromTime.toLocalTime())
    var yearMonth = YearMonth.from(dateTime)

    val interval = if (reminder.repeatInterval <= 0L) {
      1L
    } else {
      reminder.repeatInterval
    }

    var lastDay = yearMonth.atEndOfMonth().dayOfMonth
    dateTime = if (dayOfMonth <= lastDay) {
      dateTime.withDayOfMonth(dayOfMonth)
    } else {
      dateTime.withDayOfMonth(lastDay)
    }
    var isAfter = dateTime.minusMillis(beforeValue).isAfter(fromTime)
    if (isAfter) {
      return dateTime.withSecond(0)
    }
    while (true) {
      isAfter = dateTime.minusMillis(beforeValue).isAfter(fromTime)
      if (isAfter) {
        break
      }

      dateTime = dateTime.withDayOfMonth(1).plusMonths(interval)

      yearMonth = YearMonth.from(dateTime)
      lastDay = yearMonth.atEndOfMonth().dayOfMonth
      dateTime = if (dayOfMonth <= lastDay) {
        dateTime.withDayOfMonth(dayOfMonth)
      } else {
        dateTime.withDayOfMonth(lastDay)
      }
    }
    return dateTime.withSecond(0)
  }

  private fun getLastMonthDayTime(fromTime: LocalDateTime, reminder: Reminder): LocalDateTime {
    val startDateTime = fromGmtToLocal(reminder.eventTime) ?: getCurrentDateTime()
    var dateTime = LocalDateTime.of(startDateTime.toLocalDate(), fromTime.toLocalTime())

    val interval = if (reminder.repeatInterval <= 0L) {
      1L
    } else {
      reminder.repeatInterval
    }
    var yearMonth: YearMonth?

    while (true) {
      yearMonth = YearMonth.from(dateTime)
      dateTime = dateTime.withDayOfMonth(yearMonth.atEndOfMonth().dayOfMonth)

      if (dateTime.minusMillis(reminder.remindBefore).isAfter(fromTime)) {
        break
      }

      dateTime = dateTime.withDayOfMonth(1)
        .plusMonths(interval)
    }
    return dateTime.withSecond(0)
  }

  fun getNextYearDayTime(
    reminder: Reminder,
    fromTime: LocalDateTime = getCurrentDateTime()
  ): LocalDateTime {
    val dayOfMonth = reminder.dayOfMonth
    val monthOfYear = reminder.monthOfYear + 1
    val beforeValue = reminder.remindBefore

    val startDateTime = fromGmtToLocal(reminder.eventTime) ?: getCurrentDateTime()

    var dateTime = LocalDateTime.of(startDateTime.toLocalDate(), fromTime.toLocalTime())
      .withMonth(monthOfYear)
    var yearMonth = YearMonth.from(dateTime)

    dateTime = if (dayOfMonth <= yearMonth.atEndOfMonth().dayOfMonth) {
      dateTime.withDayOfMonth(dayOfMonth)
    } else {
      dateTime.withDayOfMonth(yearMonth.atEndOfMonth().dayOfMonth)
    }

    if (dateTime.minusMillis(beforeValue) <= fromTime) {
      while (true) {
        if (dateTime.minusMillis(beforeValue) > fromTime) {
          break
        }
        dateTime = dateTime.plusYears(1)
          .withDayOfMonth(1)

        yearMonth = YearMonth.from(dateTime)
        dateTime = if (dayOfMonth <= yearMonth.atEndOfMonth().dayOfMonth) {
          dateTime.withDayOfMonth(dayOfMonth)
        } else {
          dateTime.withDayOfMonth(yearMonth.atEndOfMonth().dayOfMonth)
        }
      }
    }
    return dateTime.withSecond(0)
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

  fun generateNextTimer(reminder: Reminder, isNew: Boolean): LocalDateTime {
    val hours = reminder.hours
    val fromHour = reminder.from
    val toHour = reminder.to
    var dateTime = if (isNew) {
      fromMillis(System.currentTimeMillis() + reminder.after)
    } else {
      (fromGmtToLocal(reminder.eventTime) ?: getCurrentDateTime())
        .plusMillis(reminder.repeatInterval)
    }
    if (hours.isNotEmpty()) {
      while (hours.contains(dateTime.hour)) {
        dateTime = dateTime.minusMillis(reminder.repeatInterval)
      }
      return dateTime
    }

    if (fromHour.isNotEmpty() && toHour.isNotEmpty()) {
      val fromTime = toLocalTime(fromHour)
      val toTime = toLocalTime(toHour)
      val currentDate = nowDateTimeProvider.nowDate()
      if (fromTime != null && toTime != null) {
        val start = LocalDateTime.of(currentDate, fromTime)
        val end = LocalDateTime.of(currentDate, toTime)
        while (isRange(dateTime, start, end)) {
          dateTime = dateTime.plusSeconds(reminder.repeatInterval / 1000L)
        }
      }
    }
    return dateTime
  }

  private fun isRange(dateTime: LocalDateTime, start: LocalDateTime, end: LocalDateTime): Boolean {
    return if (start > end) {
      dateTime.isAfter(start) && dateTime.isBefore(end)
    } else {
      dateTime.isAfter(start) && dateTime.isBefore(end)
    }
  }

  fun getNextWeekdayTime(
    reminder: Reminder,
    fromTime: LocalDateTime = getCurrentDateTime()
  ): LocalDateTime {
    val weekdays = reminder.weekdays
    val beforeValue = reminder.remindBefore

    var dateTIme = fromGmtToLocal(reminder.eventTime) ?: getCurrentDateTime()

    while (true) {
      if (weekdays[localDayOfWeekToOld(dateTIme.dayOfWeek) - 1] == 1 &&
        dateTIme.minusMillis(beforeValue) > fromTime
      ) {
        break
      }
      dateTIme = dateTIme.plusDays(1)
    }
    return dateTIme
  }

  fun oldDayOfWeekToLocal(dayOfWeek: Int): Int {
    // sunday = 1 - saturday = 7
    return if (dayOfWeek == 1) {
      DayOfWeek.SUNDAY.value
    } else {
      dayOfWeek - 1
    }
  }

  fun localDayOfWeekToOld(dayOfWeek: DayOfWeek): Int {
    // monday = 1 - sunday = 7
    return if (dayOfWeek == DayOfWeek.SUNDAY) {
      1
    } else {
      dayOfWeek.value + 1
    }
  }

  fun getNextWeekdayTime(
    startTime: LocalDateTime,
    weekdays: List<Int>,
    delay: Long
  ): LocalDateTime {
    var dateTime = startTime.withSecond(0)
    return if (delay > 0) {
      startTime.plusMinutes(delay)
    } else {
      val now = LocalDateTime.now()
      while (true) {
        if (weekdays[localDayOfWeekToOld(dateTime.dayOfWeek) - 1] == 1 && dateTime > now) {
          break
        }
        dateTime = dateTime.plusDays(1)
      }
      dateTime
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

  fun getVoiceDateTime(date: String?): String? {
    if (date.isNullOrEmpty()) return null
    val loc = Locale(language.getTextLanguage(prefs.voiceLocale))
    val formatter = if (prefs.voiceLocale == 0) {
      if (prefs.is24HourFormat) {
        DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy HH:mm", loc)
      } else {
        DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy h:mm a", loc)
      }
    } else {
      if (prefs.is24HourFormat) {
        DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm", loc)
      } else {
        DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy h:mm a", loc)
      }
    }
    return fromGmtToLocal(date)?.format(formatter)
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

  fun simpleDateFormatter(): DateTimeFormatter = localizedDateFormatter("d MMMM")

  private fun headerDateFormatter(): DateTimeFormatter = localizedDateFormatter("d MMMM yyyy")

  private fun dayMonthBirthdayUiFormatter(): DateTimeFormatter =
    localizedDateFormatter("d MMMM")

  private fun dateFormatter(): DateTimeFormatter = localizedDateFormatter("dd MMM yyyy")

  private fun birthdaySearchDayMonth(): DateTimeFormatter = localizedDateFormatter("dd|MM")

  private fun calendarFullDate(): DateTimeFormatter = localizedDateFormatter("MMMM dd, yyyy")

  private fun calendarMonthYear(): DateTimeFormatter = localizedDateFormatter("MMMM yyyy")

  private fun shortWeekDay(): DateTimeFormatter = localizedDateFormatter("EEE")

  companion object {
    const val SECOND: Long = 1000
    const val MINUTE: Long = 60 * SECOND
    const val HOUR: Long = MINUTE * 60
    private const val HALF_DAY: Long = HOUR * 12
    const val DAY: Long = HALF_DAY * 2
    const val WEEK: Long = DAY * 7

    private const val GMT = "GMT"
    private val GMT_ZONE_ID = ZoneId.of(GMT)
    val UTC_ZONE_ID = ZoneId.of("UTC")

    private val RFC3339_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    private val BIRTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val BIRTH_SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd", Locale.US)
    private val VOICE_ENGINE_GMT_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val GMT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
    private const val FIRE_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
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
  }

  data class Date(val year: Int, val month: Int, val day: Int)
  data class Time(val hour: Int, val minute: Int, val second: Int)
  data class BirthDate(val dateTime: LocalDateTime, val year: Int)
}
