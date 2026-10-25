package com.github.naz013.logic.habit

import org.threeten.bp.LocalDate

/** One calendar day of a habit's history; [level] is 0 (nothing logged) to [MAX_LEVEL] (target met). */
data class HabitDay(
  val date: LocalDate,
  val count: Int,
  val level: Int
) {
  companion object {
    const val MAX_LEVEL = 4
  }
}
