package com.github.naz013.domain.reminder.v2

import org.threeten.bp.LocalDateTime

sealed class RecurrenceRule {
  data object Once : RecurrenceRule()

  /** Fires once after [after] millis. [repeatInterval] (raw millis, same convention as [Daily])
   * optionally repeats the timer after that; 0 (the default) means fire once and stop. */
  data class Countdown(
    val after: Long,
    val repeatInterval: Long = 0,
    val repeatLimit: Int = -1
  ) : RecurrenceRule()

  /** Plain "repeat every X" reminder with no calendar-unit meaning. Unlike [Weekly]/[Monthly]/
   * [Yearly]'s [repeatInterval] (a small integer count of that unit), [repeatInterval] here is a
   * raw millisecond duration - it can express sub-day granularity (seconds/minutes/hours), not
   * just whole days, matching how V1's plain by-date repeat picker stores its value. */
  data class Daily(
    val repeatInterval: Long = 1,
    val repeatLimit: Int = -1,
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  data class Weekly(
    val weekdays: List<Int>,
    val repeatInterval: Long = 1,
    val repeatLimit: Int = -1,
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  data class Monthly(
    val dayOfMonth: Int,
    val repeatInterval: Long = 1,
    val repeatLimit: Int = -1,
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  /** e.g. "every 2nd Tuesday of the month". [weekday] follows the app's 0=Sunday..6=Saturday convention. */
  data class RelativeMonthly(
    val weekday: Int,
    val ordinal: Int,
    val repeatInterval: Long = 1,
    val repeatLimit: Int = -1,
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  data class Yearly(
    val dayOfMonth: Int,
    val monthOfYear: Int,
    val repeatInterval: Long = 1,
    val repeatLimit: Int = -1,
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  data object LocationEnter : RecurrenceRule()

  data object LocationExit : RecurrenceRule()

  /** Escape hatch for anything the structured cases above don't model, e.g. BYSETPOS/BYWEEKNO combinations. */
  data class ICalendar(
    val rrule: String
  ) : RecurrenceRule()
}

/** The repeat-limit count for whichever variant is active, or -1 (V1's "unlimited" convention) for
 * variants that don't carry one ([Once]/[LocationEnter]/[LocationExit]/[ICalendar]). */
fun RecurrenceRule.repeatLimitOrDefault(): Int = when (this) {
  is RecurrenceRule.Daily -> repeatLimit
  is RecurrenceRule.Weekly -> repeatLimit
  is RecurrenceRule.Monthly -> repeatLimit
  is RecurrenceRule.RelativeMonthly -> repeatLimit
  is RecurrenceRule.Yearly -> repeatLimit
  is RecurrenceRule.Countdown -> repeatLimit
  RecurrenceRule.Once,
  RecurrenceRule.LocationEnter,
  RecurrenceRule.LocationExit,
  is RecurrenceRule.ICalendar,
  -> -1
}
