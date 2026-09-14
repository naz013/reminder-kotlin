package com.github.naz013.logic.quickadd

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.threeten.bp.LocalDateTime

/** Outcome of [QuickAddParser.parse]. */
sealed class QuickAddResult {

  /** Everything needed to save a [com.github.naz013.domain.reminder.v2.ReminderV2] directly,
   * skipping the build-reminder wizard. [matchedRanges] are the substrings of the original input
   * recognized as date/time/recurrence, for live highlighting in the input field. */
  data class Parsed(
    val title: String,
    val startDateTime: LocalDateTime,
    val recurrence: RecurrenceRule,
    val matchedRanges: List<IntRange>,
  ) : QuickAddResult()

  /** Some part of the schedule was recognized but the result isn't save-able as-is - either no
   * title text is left after removing the recognized date/time/recurrence phrase, or nothing at
   * all was recognized in a language this grammar covers. Callers should hand [title] (and
   * [startDateTime]/[recurrence] when present) off to the full build-reminder wizard. */
  data class PartiallyParsed(
    val title: String,
    val startDateTime: LocalDateTime?,
    val recurrence: RecurrenceRule?,
    val matchedRanges: List<IntRange>,
  ) : QuickAddResult()

  /** The input was blank, or no [QuickAddGrammar] is registered for the requested language.
   * Callers should fall back to the full build-reminder wizard with the raw text as the title. */
  data object Empty : QuickAddResult()
}
