package com.github.naz013.feature.calendar

/**
 * The four ways the calendar can render the same underlying data. Persisted (by name) via
 * [CalendarPreferences.lastViewMode] so the last-used mode is restored on the next visit.
 *
 * [daySpan] is the number of day columns a vertical-timeline mode shows; [MONTH] and [DAY]
 * aren't timelines and report a span of 1 for convenience only.
 */
enum class CalendarViewMode(val daySpan: Int) {
  MONTH(daySpan = 1),
  DAY(daySpan = 1),
  THREE_DAY(daySpan = 3),
  SEVEN_DAY(daySpan = 7),
}
