package com.github.naz013.appfunctions.birthday

import com.github.naz013.datecalc.DateTimeManager
import org.threeten.bp.LocalDate as ThreeTenLocalDate

/** The birthday fields derived purely from a date of birth. Kept in one place since
 * [com.github.naz013.domain.Birthday] stores them denormalized (`day`/`month`/`dayMonth`/`date`),
 * and both create and update need to recompute all four identically whenever the date changes. */
internal data class BirthdayDateFields(
  val date: String,
  val day: Int,
  val month: Int,
  val dayMonth: String,
)

internal fun DateTimeManager.toBirthdayDateFields(birthDate: ThreeTenLocalDate): BirthdayDateFields =
  BirthdayDateFields(
    date = formatBirthdayDate(birthDate),
    day = birthDate.dayOfMonth,
    month = birthDate.monthValue - 1,
    dayMonth = "${birthDate.dayOfMonth}|${birthDate.monthValue - 1}",
  )
