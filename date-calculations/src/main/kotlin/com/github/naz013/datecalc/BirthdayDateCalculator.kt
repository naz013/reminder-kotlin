package com.github.naz013.datecalc

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

interface BirthdayDateCalculator {
  /**
   * Calculates the next occurrence of a birthday relative to [nowDateTime].
   *
   * @param birthDate The birth date (year is only used when [ignoreYear] is false)
   * @param birthdayTime The time of day the birthday should be observed
   * @param ignoreYear Whether the birth year is unknown/should be ignored
   * @param showedYear The year the birthday was last shown/celebrated (only relevant when [ignoreYear] is true)
   * @param nowDateTime The reference date/time to calculate the next occurrence from
   * @return The next occurrence of the birthday, safe for Feb 29 birthdays in non-leap years
   */
  fun getNextOccurrence(
    birthDate: LocalDate,
    birthdayTime: LocalTime,
    ignoreYear: Boolean,
    showedYear: Int,
    nowDateTime: LocalDateTime,
  ): LocalDateTime

  /**
   * Calculates a window of birthday occurrences starting at [fromYear].
   *
   * @param birthDate The birth date (only month/day are used)
   * @param time The time of day the birthday should be observed
   * @param occurrenceCount The number of additional years to calculate after [fromYear] (inclusive range of occurrenceCount + 1 results)
   * @param fromYear The first year to calculate an occurrence for
   * @return One occurrence per year, each resolved independently so a Feb 29 birthday reappears in later leap years
   */
  fun getOccurrenceWindow(
    birthDate: LocalDate,
    time: LocalTime,
    occurrenceCount: Int,
    fromYear: Int,
  ): List<LocalDateTime>

  /**
   * Checks whether a birthday (shifted by [daysBefore]) falls on [targetDate], ignoring year.
   *
   * @param birthMonth1Based The birth month, 1 = January
   * @param birthDay The birth day of month
   * @param targetDate The date to check against
   * @param daysBefore Number of days to shift the birthday earlier before comparing
   */
  fun isBirthdayOn(
    birthMonth1Based: Int,
    birthDay: Int,
    targetDate: LocalDate,
    daysBefore: Int,
  ): Boolean

  /**
   * Calculates the age in whole years between [birthDate] and [nowDate].
   */
  fun getAge(
    birthDate: LocalDate,
    nowDate: LocalDate,
  ): Int
}
