package com.github.naz013.datecalc

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.abs

internal class BirthdayDateCalculatorImpl : BirthdayDateCalculator {
  override fun getNextOccurrence(
    birthDate: LocalDate,
    birthdayTime: LocalTime,
    ignoreYear: Boolean,
    showedYear: Int,
    nowDateTime: LocalDateTime,
  ): LocalDateTime {
    var dateTime = LocalDateTime.of(safeDateFor(nowDateTime.year, birthDate.monthValue, birthDate.dayOfMonth), birthdayTime)
    if (dateTime.isBefore(nowDateTime) && !ignoreYear) {
      dateTime =
        LocalDateTime.of(
          safeDateFor(nowDateTime.year + 1, birthDate.monthValue, birthDate.dayOfMonth),
          birthdayTime,
        )
    } else if (dateTime.isBefore(nowDateTime) && ignoreYear && showedYear >= dateTime.year) {
      dateTime =
        LocalDateTime.of(
          safeDateFor(nowDateTime.year + 1, birthDate.monthValue, birthDate.dayOfMonth),
          birthdayTime,
        )
    }
    return dateTime
  }

  override fun getOccurrenceWindow(
    birthDate: LocalDate,
    time: LocalTime,
    occurrenceCount: Int,
    fromYear: Int,
  ): List<LocalDateTime> =
    (0..occurrenceCount).map { i ->
      LocalDateTime.of(safeDateFor(fromYear + i, birthDate.monthValue, birthDate.dayOfMonth), time)
    }

  override fun isBirthdayOn(
    birthMonth1Based: Int,
    birthDay: Int,
    targetDate: LocalDate,
    daysBefore: Int,
  ): Boolean {
    val candidate = safeDateFor(targetDate.year, birthMonth1Based, birthDay).minusDays(daysBefore.toLong())
    return candidate.dayOfMonth == targetDate.dayOfMonth && candidate.monthValue == targetDate.monthValue
  }

  override fun getAge(
    birthDate: LocalDate,
    nowDate: LocalDate,
  ): Int = abs(ChronoUnit.YEARS.between(birthDate, nowDate).toInt())

  /**
   * Builds a date for [year]/[month]/[day], clamping [day] to the last valid day of [month] in
   * [year] instead of throwing (e.g. Feb 29 in a non-leap year resolves to Feb 28).
   */
  private fun safeDateFor(
    year: Int,
    month: Int,
    day: Int,
  ): LocalDate {
    val lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth()
    return LocalDate.of(year, month, minOf(day, lastDayOfMonth))
  }
}

fun provideBirthdayDateCalculator(): BirthdayDateCalculator {
  return BirthdayDateCalculatorImpl()
}
