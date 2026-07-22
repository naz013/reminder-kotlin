package com.github.naz013.domain.reminder.v2

sealed class RecurrenceRule {
  data object Once : RecurrenceRule()

  data class Countdown(
    val after: Long
  ) : RecurrenceRule()

  data class Weekly(
    val weekdays: List<Int>
  ) : RecurrenceRule()

  data class Monthly(
    val dayOfMonth: Int,
    val repeatInterval: Long,
    val repeatLimit: Int
  ) : RecurrenceRule()

  data class Yearly(
    val dayOfMonth: Int,
    val monthOfYear: Int
  ) : RecurrenceRule()

  data object LocationEnter : RecurrenceRule()

  data object LocationExit : RecurrenceRule()

  data class ICalendar(
    val rrule: String
  ) : RecurrenceRule()
}
