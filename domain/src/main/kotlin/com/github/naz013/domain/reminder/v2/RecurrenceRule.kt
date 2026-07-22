package com.github.naz013.domain.reminder.v2

import org.threeten.bp.LocalDateTime

sealed class RecurrenceRule {
  data object Once : RecurrenceRule()

  data class Countdown(
    val after: Long
  ) : RecurrenceRule()

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
