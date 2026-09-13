package com.github.naz013.logic.quickadd

import org.threeten.bp.LocalTime

/** Per-language recognizer for the pieces of a quick-add phrase. Each `extract*` function scans
 * the full, untouched input independently and returns the first/best match with its exact
 * character range - [QuickAddParser] is responsible for combining the results, resolving them
 * against an anchor date, and cutting matched ranges out of the input to obtain the title. */
interface QuickAddGrammar {
  val languageTag: String

  fun extractRecurrence(input: String): QuickAddMatch<RecurrenceSignal>?

  fun extractTime(input: String): QuickAddMatch<LocalTime>?

  fun extractDate(input: String): QuickAddMatch<ParsedDate>?

  /** Strips language-specific dangling connector words/punctuation left at the edges of the
   * title once matched ranges have been cut out (e.g. "call mom on" -> "call mom"). */
  fun cleanTitle(rawTitle: String): String
}
