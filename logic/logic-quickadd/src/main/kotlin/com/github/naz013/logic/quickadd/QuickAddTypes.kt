package com.github.naz013.logic.quickadd

/** A single recognized phrase: the extracted [value] plus its character range in the original,
 * untouched input string (used both to strip it out of the leftover title and to highlight it live). */
data class QuickAddMatch<T>(val value: T, val range: IntRange)

/** What a recurrence phrase ("every day", "every 1st", "every monday") tells us, before it's
 * resolved against an anchor date into a concrete [com.github.naz013.domain.reminder.v2.RecurrenceRule].
 * A `null`/empty field means the phrase didn't specify that part, and it should be derived from
 * whatever date was separately recognized (or from the resolved start date as a last resort). */
sealed class RecurrenceSignal {
  data class Daily(val interval: Long = 1) : RecurrenceSignal()

  data class Weekly(val weekdays: List<Int>, val interval: Long = 1) : RecurrenceSignal()

  data class Monthly(val dayOfMonth: Int?, val interval: Long = 1) : RecurrenceSignal()

  data class Yearly(val interval: Long = 1) : RecurrenceSignal()
}

/** A date phrase, not yet resolved against "now". [NextWeekday.weekday] follows the app's
 * 0=Sunday..6=Saturday convention (see [com.github.naz013.domain.reminder.v2.RecurrenceRule.RelativeMonthly]). */
sealed class ParsedDate {
  data class Absolute(val month: Int, val day: Int, val year: Int? = null) : ParsedDate()

  data object Today : ParsedDate()

  data object Tomorrow : ParsedDate()

  /** [forceNextWeek] distinguishes "monday" (today, if today is Monday) from "next monday"
   * (always at least a week out). */
  data class NextWeekday(val weekday: Int, val forceNextWeek: Boolean = false) : ParsedDate()
}
