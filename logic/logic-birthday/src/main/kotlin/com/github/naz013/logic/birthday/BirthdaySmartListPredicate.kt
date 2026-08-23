package com.github.naz013.logic.birthday

import com.github.naz013.datecalc.BirthdayDateCalculator
import com.github.naz013.domain.Birthday
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import org.threeten.bp.LocalDate

/**
 * Birthdays recur every year, so they're matched by month/day rather than a single due date -
 * OVERDUE has no meaning here and never matches. NO_GROUP is vacuously true: birthdays have no
 * group concept at all, so a group-based filter shouldn't hide them.
 */
class BirthdaySmartListPredicate(
  private val birthdayDateCalculator: BirthdayDateCalculator,
) {
  fun matches(
    filter: SmartListFilter,
    birthday: Birthday,
    today: LocalDate,
  ): Boolean {
    return when (filter) {
      SmartListFilter.TODAY -> isBirthdayOn(birthday, today)
      SmartListFilter.OVERDUE -> false
      SmartListFilter.THIS_WEEK -> (0..6).any { isBirthdayOn(birthday, today.plusDays(it.toLong())) }
      SmartListFilter.NO_GROUP -> true
    }
  }

  private fun isBirthdayOn(
    birthday: Birthday,
    date: LocalDate,
  ): Boolean =
    birthdayDateCalculator.isBirthdayOn(
      birthMonth1Based = birthday.month + 1,
      birthDay = birthday.day,
      targetDate = date,
      daysBefore = 0,
    )
}
