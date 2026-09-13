package com.github.naz013.logic.quickadd.vocabulary

/** Whether this language writes an absolute date as "month day" (English: "august 5") or
 * "day month" (most of Tier 1: "5 august"/"5 de agosto"/"5. august"). */
enum class DateOrder {
  MONTH_DAY,
  DAY_MONTH,
}

/** The per-language word tables [com.github.naz013.logic.quickadd.grammar.KeywordQuickAddGrammar]
 * plugs into its shared regex skeleton. Weekday and month names themselves are NOT listed here -
 * they're sourced from the JDK/ThreeTen locale data for [languageTag] at grammar construction
 * time, so they're always grammatically correct without needing manual translation.
 *
 * Coverage is deliberately v1-scoped to the common phrasings from the feature spec ("every 1st at
 * 9am", "every monday", "tomorrow at 10am") - not exhaustive natural-language coverage. Anything
 * a word list here doesn't recognize simply isn't matched, and the phrase falls back to the full
 * build-reminder wizard rather than producing a wrong guess. */
data class QuickAddVocabulary(
  val languageTag: String,
  val everyWords: List<String>,
  val dailyUnitWords: List<String>,
  val weeklyUnitWords: List<String>,
  val monthlyUnitWords: List<String>,
  val yearlyUnitWords: List<String>,
  val dailyAdverbs: List<String>,
  val weeklyAdverbs: List<String>,
  val monthlyAdverbs: List<String>,
  val yearlyAdverbs: List<String>,
  val workdayWords: List<String>,
  /** Regex alternation (no capturing group) for the ordinal marker following a bare day number in
   * an "every <n><suffix>" recurrence phrase, e.g. "st|nd|rd|th" for English. Empty if this
   * language just uses the bare number. */
  val ordinalSuffixRegex: String,
  val todayWords: List<String>,
  val tomorrowWords: List<String>,
  val nextWords: List<String>,
  /** Preposition/article preceding a weekday name, e.g. English "on", Spanish "el"/"la". */
  val onWords: List<String>,
  /** Preposition introducing a clock time, e.g. English "at", German "um". */
  val atWords: List<String>,
  val amWords: List<String>,
  val pmWords: List<String>,
  val noonWords: List<String>,
  val midnightWords: List<String>,
  /** Trailing time unit word consumed (but not required) after a clock time, e.g. German "Uhr",
   * English "o'clock". */
  val timeUnitWords: List<String>,
  val dateOrder: DateOrder,
  /** Optional connector between day and month in [DateOrder.DAY_MONTH], e.g. Spanish/Portuguese "de". */
  val dayMonthConnectors: List<String>,
  /** Dangling prepositions stripped from the edges of the leftover title. */
  val connectorWords: List<String>,
)
