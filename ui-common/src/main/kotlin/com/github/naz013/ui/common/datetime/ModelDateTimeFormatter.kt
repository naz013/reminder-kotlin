package com.github.naz013.ui.common.datetime

import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.datetime.DateTimePreferences
import com.github.naz013.common.datetime.NowDateTimeProvider
import com.github.naz013.datecalc.BirthdayDateCalculator
import com.github.naz013.datecalc.BirthdayDateCalculatorImpl
import com.github.naz013.domain.Birthday
import com.github.naz013.ui.common.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit

class ModelDateTimeFormatter(
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  private val dateTimePreferences: DateTimePreferences,
  private val nowDateTimeProvider: NowDateTimeProvider = NowDateTimeProvider(),
  private val birthdayDateCalculator: BirthdayDateCalculator = BirthdayDateCalculatorImpl(),
) {
  fun getRemaining(
    dateTime: String?,
    delay: Int,
  ): String {
    if (dateTime.isNullOrEmpty()) {
      return getRemaining(null)
    }
    return getRemaining(dateTimeManager.fromGmtToLocal(dateTime)?.plusMinutes(delay.toLong()))
  }

  fun getBirthdayRemaining(
    futureBirthdayDateTime: LocalDateTime,
    ignoreYear: Boolean,
    nowDateTime: LocalDateTime = nowDateTimeProvider.nowDateTime(),
  ): String? =
    when {
      ignoreYear -> null
      futureBirthdayDateTime == nowDateTime -> null
      futureBirthdayDateTime.isBefore(nowDateTime) -> {
        textProvider.getText(R.string.not_born)
      }

      else -> getRemaining(futureBirthdayDateTime, nowDateTime)
    }

  fun getRemaining(
    eventTime: LocalDateTime?,
    nowDateTime: LocalDateTime = nowDateTimeProvider.nowDateTime(),
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
    nowDateTime: LocalDateTime = nowDateTimeProvider.nowDateTime(),
  ): LocalDateTime =
    birthdayDateCalculator.getNextOccurrence(
      birthDate = birthdayDate,
      birthdayTime = birthdayTime,
      ignoreYear = birthday.ignoreYear,
      showedYear = birthday.showedYear,
      nowDateTime = nowDateTime,
    )

  fun getAgeFormatted(
    date: String?,
    nowDate: LocalDate = nowDateTimeProvider.nowDate(),
  ): String {
    val years = getAge(date, nowDate)
    val language = dateTimePreferences.locale.language.lowercase()
    return buildYearString(language, years)
  }

  private fun getAge(
    dateOfBirth: String?,
    nowDate: LocalDate,
  ): Int {
    if (dateOfBirth.isNullOrEmpty()) return 0
    val birthDate = dateTimeManager.parseBirthdayDate(dateOfBirth) ?: return 0
    return birthdayDateCalculator.getAge(birthDate, nowDate)
  }

  private fun buildYearString(
    language: String,
    years: Int,
  ): String =
    if (language.startsWith("uk")) {
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
