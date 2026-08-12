package com.github.naz013.domain.reminder.v2

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

/** Variants are Gson round-tripped directly (see `ReminderV2Mapper`/`files/DataConverterImpl`),
 * so every field needs [SerializedName] - without it R8 is free to strip/rename the constructor
 * and fields, which crashed in production (see the "Failed to invoke constructor" incident). */
sealed class RecurrenceRule {
  data object Once : RecurrenceRule()

  /** Fires once after [after] millis. [repeatInterval] (raw millis, same convention as [Daily])
   * optionally repeats the timer after that; 0 (the default) means fire once and stop. */
  data class Countdown(
    @SerializedName("after")
    val after: Long,
    @SerializedName("repeatInterval")
    val repeatInterval: Long = 0,
    @SerializedName("repeatLimit")
    val repeatLimit: Int = -1
  ) : RecurrenceRule()

  /** Plain "repeat every X" reminder with no calendar-unit meaning. Unlike [Weekly]/[Monthly]/
   * [Yearly]'s [repeatInterval] (a small integer count of that unit), [repeatInterval] here is a
   * raw millisecond duration - it can express sub-day granularity (seconds/minutes/hours), not
   * just whole days, matching how V1's plain by-date repeat picker stores its value. */
  data class Daily(
    @SerializedName("repeatInterval")
    val repeatInterval: Long = 1,
    @SerializedName("repeatLimit")
    val repeatLimit: Int = -1,
    @SerializedName("until")
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  data class Weekly(
    @SerializedName("weekdays")
    val weekdays: List<Int>,
    @SerializedName("repeatInterval")
    val repeatInterval: Long = 1,
    @SerializedName("repeatLimit")
    val repeatLimit: Int = -1,
    @SerializedName("until")
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  data class Monthly(
    @SerializedName("dayOfMonth")
    val dayOfMonth: Int,
    @SerializedName("repeatInterval")
    val repeatInterval: Long = 1,
    @SerializedName("repeatLimit")
    val repeatLimit: Int = -1,
    @SerializedName("until")
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  /** e.g. "every 2nd Tuesday of the month". [weekday] follows the app's 0=Sunday..6=Saturday convention. */
  data class RelativeMonthly(
    @SerializedName("weekday")
    val weekday: Int,
    @SerializedName("ordinal")
    val ordinal: Int,
    @SerializedName("repeatInterval")
    val repeatInterval: Long = 1,
    @SerializedName("repeatLimit")
    val repeatLimit: Int = -1,
    @SerializedName("until")
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  data class Yearly(
    @SerializedName("dayOfMonth")
    val dayOfMonth: Int,
    @SerializedName("monthOfYear")
    val monthOfYear: Int,
    @SerializedName("repeatInterval")
    val repeatInterval: Long = 1,
    @SerializedName("repeatLimit")
    val repeatLimit: Int = -1,
    @SerializedName("until")
    val until: LocalDateTime? = null
  ) : RecurrenceRule()

  data object LocationEnter : RecurrenceRule()

  data object LocationExit : RecurrenceRule()

  /** Escape hatch for anything the structured cases above don't model, e.g. BYSETPOS/BYWEEKNO combinations. */
  data class ICalendar(
    @SerializedName("rrule")
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
