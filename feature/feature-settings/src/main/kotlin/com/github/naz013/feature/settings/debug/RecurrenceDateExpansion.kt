package com.github.naz013.feature.settings.debug

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.threeten.bp.LocalDate

/**
 * Approximates the dates [recurrence] would fire on between [windowStart] and [windowEnd]
 * (inclusive), anchored at [anchorDate]. Used only by [PopulateCalendarDemoDataUseCase] to
 * synthesize [com.github.naz013.domain.occurance.EventOccurrence] rows directly for demo data,
 * bypassing the real `CalculateReminderOccurrencesUseCase` (which resolves one reminder at a time
 * via WorkManager - impractical at [PopulateCalendarDemoDataUseCase.Scale.MASSIVE] volumes). This
 * doesn't need to match production's recurrence math exactly (no `until`/`repeatLimit` handling,
 * no DST-aware sub-day `Daily` intervals) - just enough to look plausible in the calendar UI.
 */
internal fun expandRecurrenceDates(
  recurrence: RecurrenceRule,
  anchorDate: LocalDate,
  windowStart: LocalDate,
  windowEnd: LocalDate,
): List<LocalDate> {
  val effectiveStart = maxOf(anchorDate, windowStart)
  if (effectiveStart.isAfter(windowEnd)) return emptyList()

  return when (recurrence) {
    is RecurrenceRule.Daily ->
      generateSequence(effectiveStart) { it.plusDays(1) }
        .takeWhile { !it.isAfter(windowEnd) }
        .toList()

    is RecurrenceRule.Weekly -> {
      val activeWeekdays =
        recurrence.weekdays
          .mapIndexedNotNull { index, value -> if (value == 1) index else null }
          .toSet()
      generateSequence(effectiveStart) { it.plusDays(1) }
        .takeWhile { !it.isAfter(windowEnd) }
        .filter { weekdayIndex(it) in activeWeekdays }
        .toList()
    }

    is RecurrenceRule.Monthly ->
      generateSequence(anchorDate.withDayOfMonth(1)) { it.plusMonths(1) }
        .map { monthStart -> monthStart.withDayOfMonth(minOf(recurrence.dayOfMonth, monthStart.lengthOfMonth())) }
        .dropWhile { it.isBefore(effectiveStart) }
        .takeWhile { !it.isAfter(windowEnd) }
        .toList()

    is RecurrenceRule.RelativeMonthly ->
      generateSequence(anchorDate.withDayOfMonth(1)) { it.plusMonths(1) }
        .mapNotNull { monthStart -> nthWeekdayOfMonth(monthStart, recurrence.weekday, recurrence.ordinal) }
        .dropWhile { it.isBefore(effectiveStart) }
        .takeWhile { !it.isAfter(windowEnd) }
        .toList()

    is RecurrenceRule.Yearly ->
      (windowStart.year..windowEnd.year)
        .mapNotNull { year -> runCatching { LocalDate.of(year, recurrence.monthOfYear + 1, recurrence.dayOfMonth) }.getOrNull() }
        .filter { !it.isBefore(effectiveStart) && !it.isAfter(windowEnd) }

    RecurrenceRule.Once,
    is RecurrenceRule.Countdown,
    RecurrenceRule.LocationEnter,
    RecurrenceRule.LocationExit,
    is RecurrenceRule.ICalendar,
    -> if (!anchorDate.isBefore(windowStart) && !anchorDate.isAfter(windowEnd)) listOf(anchorDate) else emptyList()
  }
}

/** The nth (1-based [ordinal]) occurrence of [weekday] (app's 0=Sunday..6=Saturday) in [monthStart]'s
 * month, or null if that ordinal doesn't exist in the month (e.g. a 5th Monday that isn't there). */
internal fun nthWeekdayOfMonth(
  monthStart: LocalDate,
  weekday: Int,
  ordinal: Int,
): LocalDate? {
  val firstMatch = generateSequence(monthStart) { it.plusDays(1) }.first { weekdayIndex(it) == weekday }
  val candidate = firstMatch.plusWeeks((ordinal - 1).toLong())
  return candidate.takeIf { it.monthValue == monthStart.monthValue }
}

/** java.time's `DayOfWeek.value` is MONDAY=1..SUNDAY=7; `% 7` remaps it to the app's own
 * 0=Sunday..6=Saturday convention (see `WeekDaysProtocol.getSelectedDaysOfWeek`). */
private fun weekdayIndex(date: LocalDate): Int = date.dayOfWeek.value % 7
