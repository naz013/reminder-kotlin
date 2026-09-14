package com.github.naz013.logic.quickadd

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/** Parses a single free-text quick-add phrase (e.g. "pay rent every 1st at 9am") into a
 * ready-to-save reminder, using whichever [QuickAddGrammar] matches the caller's language.
 * Stateless and side-effect free - safe to keep a single shared instance. */
class QuickAddParser(
  grammars: List<QuickAddGrammar> = QuickAddGrammars.supported(),
) {
  private val grammarsByLanguage = grammars.associateBy { it.languageTag }

  /** [languageTag] should be the app's own language setting (not necessarily the device locale),
   * so quick-add understands the language the user chose for the app. [now] is the resolution
   * anchor for relative phrases ("tomorrow", "next monday") - pass the real current time, a fixed
   * value only in tests. */
  fun parse(input: String, languageTag: String, now: LocalDateTime): QuickAddResult {
    val grammar = grammarsByLanguage[languageTag] ?: return QuickAddResult.Empty
    val trimmedInput = input.trim()
    return if (trimmedInput.isEmpty()) QuickAddResult.Empty else resolve(grammar, trimmedInput, now)
  }

  private fun resolve(grammar: QuickAddGrammar, trimmedInput: String, now: LocalDateTime): QuickAddResult {
    val recurrenceMatch = grammar.extractRecurrence(trimmedInput)
    val timeMatch = grammar.extractTime(trimmedInput)
    val dateMatch = grammar.extractDate(trimmedInput)
    val matchedRanges = listOfNotNull(recurrenceMatch?.range, timeMatch?.range, dateMatch?.range)

    if (matchedRanges.isEmpty()) {
      return QuickAddResult.PartiallyParsed(
        title = trimmedInput,
        startDateTime = null,
        recurrence = null,
        matchedRanges = emptyList(),
      )
    }

    val title = grammar.cleanTitle(stripRanges(trimmedInput, matchedRanges))
    val startDateTime = resolveStartDateTime(now, dateMatch?.value, timeMatch?.value, recurrenceMatch?.value)
    val recurrenceRule = resolveRecurrenceRule(recurrenceMatch?.value, startDateTime)

    return if (title.isBlank()) {
      QuickAddResult.PartiallyParsed(
        title = "",
        startDateTime = startDateTime,
        recurrence = recurrenceRule,
        matchedRanges = matchedRanges,
      )
    } else {
      QuickAddResult.Parsed(
        title = title,
        startDateTime = startDateTime,
        recurrence = recurrenceRule,
        matchedRanges = matchedRanges,
      )
    }
  }

  private fun resolveStartDateTime(
    now: LocalDateTime,
    date: ParsedDate?,
    time: LocalTime?,
    recurrence: RecurrenceSignal?,
  ): LocalDateTime {
    val isRecurring = recurrence != null
    val timeOfDay = time ?: LocalTime.of(DEFAULT_HOUR, DEFAULT_MINUTE)
    val today = now.toLocalDate()
    val baseDate = resolveBaseDate(today, date, recurrence)
    val candidate = LocalDateTime.of(baseDate, timeOfDay)
    // A one-off reminder that already lapsed today only makes sense pushed to its next natural
    // occurrence; a recurring one is fine anchored in the past - the occurrence calculator finds
    // the next instance from "now" regardless. An explicit absolute date is trusted as-is.
    return if (!isRecurring && candidate.isBefore(now)) rollPastLapsedCandidate(candidate, date) else candidate
  }

  private fun resolveBaseDate(today: LocalDate, date: ParsedDate?, recurrence: RecurrenceSignal?): LocalDate =
    when {
      date is ParsedDate.Absolute -> resolveAbsoluteDate(today, date)
      date is ParsedDate.Today -> today
      date is ParsedDate.Tomorrow -> today.plusDays(1)
      date is ParsedDate.NextWeekday -> resolveWeekdayDate(today, date.weekday, date.forceNextWeek)
      // No separate date phrase - if the recurrence phrase itself named a day ("every 1st") or a
      // weekday ("every monday"), anchor to that instead of just defaulting to today.
      recurrence is RecurrenceSignal.Monthly && recurrence.dayOfMonth != null ->
        resolveMonthDayDate(today, recurrence.dayOfMonth)

      recurrence is RecurrenceSignal.Weekly && recurrence.weekdays.isNotEmpty() ->
        resolveWeekdayDate(today, recurrence.weekdays.first(), forceNextWeek = false)

      else -> today
    }

  private fun rollPastLapsedCandidate(candidate: LocalDateTime, date: ParsedDate?): LocalDateTime =
    when (date) {
      null, ParsedDate.Today -> candidate.plusDays(1)
      is ParsedDate.NextWeekday -> candidate.plusDays(7)
      else -> candidate
    }

  /** Anchors a bare "every &lt;n&gt;(st/nd/rd/th)" monthly recurrence to that day-of-month in the
   * current month, even if it has already passed - consistent with recurring anchors elsewhere in
   * this resolver, the occurrence calculator finds the next real occurrence from "now". */
  private fun resolveMonthDayDate(today: LocalDate, dayOfMonth: Int): LocalDate {
    val day = dayOfMonth.coerceIn(1, today.lengthOfMonth())
    return LocalDate.of(today.year, today.month, day)
  }

  private fun resolveAbsoluteDate(today: LocalDate, date: ParsedDate.Absolute): LocalDate {
    val year = date.year ?: today.year
    val daysInMonth = LocalDate.of(year, date.month, 1).lengthOfMonth()
    val day = date.day.coerceIn(1, daysInMonth)
    val result = LocalDate.of(year, date.month, day)
    return if (date.year == null && result.isBefore(today)) result.plusYears(1) else result
  }

  private fun resolveWeekdayDate(today: LocalDate, appWeekday: Int, forceNextWeek: Boolean): LocalDate {
    val targetIso = appWeekdayToIso(appWeekday)
    var candidate = today
    if (forceNextWeek || candidate.dayOfWeek.value != targetIso) {
      do {
        candidate = candidate.plusDays(1)
      } while (candidate.dayOfWeek.value != targetIso)
    }
    return candidate
  }

  private fun resolveRecurrenceRule(signal: RecurrenceSignal?, startDateTime: LocalDateTime): RecurrenceRule =
    when (signal) {
      null -> {
        RecurrenceRule.Once
      }

      is RecurrenceSignal.Daily -> {
        RecurrenceRule.Daily(repeatInterval = signal.interval)
      }

      is RecurrenceSignal.Weekly -> {
        val selectedDays = signal.weekdays.ifEmpty { listOf(startDateTime.dayOfWeek.toAppWeekday()) }
        RecurrenceRule.Weekly(weekdays = weekdayBitmask(selectedDays), repeatInterval = signal.interval)
      }

      is RecurrenceSignal.Monthly -> {
        RecurrenceRule.Monthly(
          dayOfMonth = signal.dayOfMonth ?: startDateTime.dayOfMonth,
          repeatInterval = signal.interval,
        )
      }

      is RecurrenceSignal.Yearly -> {
        RecurrenceRule.Yearly(
          dayOfMonth = startDateTime.dayOfMonth,
          monthOfYear = startDateTime.monthValue,
          repeatInterval = signal.interval,
        )
      }
    }

  /** [RecurrenceRule.Weekly.weekdays] is a 7-element bitmask (index 0=Sunday..6=Saturday, 1 if
   * that day repeats) - see `ByWeekdaysDecomposer`/`WeekDaysProtocol` in feature-reminder/
   * core:date-calculations, the actual consumers of this field. [selectedAppWeekdays] is the
   * parser's own natural intermediate shape (a plain list of which day indices are selected). */
  private fun weekdayBitmask(selectedAppWeekdays: List<Int>): List<Int> =
    List(7) { day -> if (day in selectedAppWeekdays) 1 else 0 }

  private fun stripRanges(text: String, ranges: List<IntRange>): String {
    val merged = mergeRanges(ranges.sortedBy { it.first })
    val builder = StringBuilder()
    var cursor = 0
    for (range in merged) {
      if (range.first > cursor) builder.append(text, cursor, range.first)
      cursor = (range.last + 1).coerceAtMost(text.length)
    }
    if (cursor < text.length) builder.append(text, cursor, text.length)
    return builder.toString().replace(WHITESPACE_RUN, " ").trim()
  }

  private fun mergeRanges(sorted: List<IntRange>): List<IntRange> {
    val merged = mutableListOf<IntRange>()
    for (range in sorted) {
      val last = merged.lastOrNull()
      if (last != null && range.first <= last.last + 1) {
        merged[merged.lastIndex] = last.first..maxOf(last.last, range.last)
      } else {
        merged.add(range)
      }
    }
    return merged
  }

  companion object {
    private const val DEFAULT_HOUR = 9
    private const val DEFAULT_MINUTE = 0
    private val WHITESPACE_RUN = Regex("\\s{2,}")
  }
}
