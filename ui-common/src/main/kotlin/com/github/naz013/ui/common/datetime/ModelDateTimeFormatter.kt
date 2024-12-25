package com.github.naz013.ui.common.datetime

import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.datetime.DateTimePreferences
import com.github.naz013.common.datetime.NowDateTimeProvider
import com.github.naz013.common.datetime.minusMillis
import com.github.naz013.common.datetime.plusMillis
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.abs

class ModelDateTimeFormatter(
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  private val dateTimePreferences: DateTimePreferences,
  private val nowDateTimeProvider: NowDateTimeProvider = NowDateTimeProvider()
) {

  fun getRemaining(dateTime: String?, delay: Int): String {
    if (dateTime.isNullOrEmpty()) {
      return getRemaining(null)
    }
    return getRemaining(dateTimeManager.fromGmtToLocal(dateTime)?.plusMinutes(delay.toLong()))
  }

  fun getBirthdayRemaining(
    futureBirthdayDateTime: LocalDateTime,
    ignoreYear: Boolean,
    nowDateTime: LocalDateTime = nowDateTimeProvider.nowDateTime()
  ): String? {
    return when {
      ignoreYear -> null
      futureBirthdayDateTime == nowDateTime -> null
      futureBirthdayDateTime.isBefore(nowDateTime) -> {
        textProvider.getText(R.string.not_born)
      }

      else -> getRemaining(futureBirthdayDateTime, nowDateTime)
    }
  }

  private fun getRemaining(
    eventTime: LocalDateTime?,
    nowDateTime: LocalDateTime = nowDateTimeProvider.nowDateTime()
  ): String {
    if (eventTime == null) return textProvider.getText(R.string.overdue)

    val days = ChronoUnit.DAYS.between(nowDateTime, eventTime)
    val hours = ChronoUnit.HOURS.between(nowDateTime, eventTime)
    val minutes = ChronoUnit.MINUTES.between(nowDateTime, eventTime)
    val seconds = ChronoUnit.SECONDS.between(nowDateTime, eventTime)

    val language = dateTimePreferences.locale.toString().lowercase()

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

  fun getFutureBirthdayDate(
    birthdayTime: LocalTime,
    birthdayDate: LocalDate,
    birthday: Birthday,
    nowDateTime: LocalDateTime = nowDateTimeProvider.nowDateTime()
  ): LocalDateTime {
    var dateTime = LocalDateTime.of(nowDateTime.toLocalDate(), birthdayTime)
      .withMonth(birthdayDate.monthValue)
      .withDayOfMonth(birthdayDate.dayOfMonth)
    if (dateTime.isBefore(nowDateTime) && !birthday.ignoreYear) {
      dateTime = dateTime.plusYears(1)
    } else if (dateTime.isBefore(nowDateTime) && birthday.ignoreYear &&
      birthday.showedYear >= dateTime.year
    ) {
      dateTime = dateTime.plusYears(1)
    }
    return dateTime
  }

  fun getAgeFormatted(
    date: String?,
    nowDate: LocalDate = nowDateTimeProvider.nowDate()
  ): String {
    val years = getAge(date, nowDate)
    val language = dateTimePreferences.locale.language.lowercase()
    return buildYearString(language, years)
  }

  private fun getAge(dateOfBirth: String?, nowDate: LocalDate): Int {
    if (dateOfBirth.isNullOrEmpty()) return 0
    val birthDate = dateTimeManager.parseBirthdayDate(dateOfBirth) ?: return 0
    return abs(ChronoUnit.YEARS.between(birthDate, nowDate).toInt())
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

  fun getNewNextMonthDayTime(
    reminder: Reminder,
    fromTime: LocalDateTime = dateTimeManager.getCurrentDateTime()
  ): LocalDateTime {
    val dayOfMonth = reminder.dayOfMonth
    val beforeValue = reminder.remindBefore

    Logger.d("getNextMonthDayTime: dayOfMonth=$dayOfMonth, before=$beforeValue, from=$fromTime")

    if (dayOfMonth == 0) {
      return getLastMonthDayTime(fromTime, reminder)
    } else if (dayOfMonth > 28) {
      return getSmartMonthDayTime(fromTime, reminder)
    }

    val startDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
      ?: dateTimeManager.getCurrentDateTime()
    var dateTime = LocalDateTime.of(startDateTime.toLocalDate(), startDateTime.toLocalTime())
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

    val startDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
      ?: dateTimeManager.getCurrentDateTime()
    var dateTime = LocalDateTime.of(startDateTime.toLocalDate(), startDateTime.toLocalTime())
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
    val startDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
      ?: dateTimeManager.getCurrentDateTime()
    var dateTime = LocalDateTime.of(startDateTime.toLocalDate(), startDateTime.toLocalTime())

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
    fromTime: LocalDateTime = dateTimeManager.getCurrentDateTime()
  ): LocalDateTime {
    val dayOfMonth = reminder.dayOfMonth
    val monthOfYear = reminder.monthOfYear + 1
    val beforeValue = reminder.remindBefore

    val startDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
      ?: dateTimeManager.getCurrentDateTime()

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

  fun generateNextTimer(reminder: Reminder, isNew: Boolean): LocalDateTime {
    val hours = reminder.hours
    val fromHour = reminder.from
    val toHour = reminder.to
    var dateTime = if (isNew) {
      dateTimeManager.fromMillis(System.currentTimeMillis() + reminder.after)
    } else {
      (dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: dateTimeManager.getCurrentDateTime())
        .plusMillis(reminder.repeatInterval)
    }
    if (hours.isNotEmpty()) {
      while (hours.contains(dateTime.hour)) {
        dateTime = dateTime.minusMillis(reminder.repeatInterval)
      }
      return dateTime
    }

    if (fromHour.isNotEmpty() && toHour.isNotEmpty()) {
      val fromTime = dateTimeManager.toLocalTime(fromHour)
      val toTime = dateTimeManager.toLocalTime(toHour)
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
    fromTime: LocalDateTime = dateTimeManager.getCurrentDateTime()
  ): LocalDateTime {
    val weekdays = reminder.weekdays
    val beforeValue = reminder.remindBefore

    var dateTIme = dateTimeManager.fromGmtToLocal(reminder.eventTime)
      ?: dateTimeManager.getCurrentDateTime()

    while (true) {
      if (weekdays[dateTimeManager.localDayOfWeekToOld(dateTIme.dayOfWeek) - 1] == 1 &&
        dateTIme.minusMillis(beforeValue) > fromTime
      ) {
        break
      }
      dateTIme = dateTIme.plusDays(1)
    }
    return dateTIme
  }
}
