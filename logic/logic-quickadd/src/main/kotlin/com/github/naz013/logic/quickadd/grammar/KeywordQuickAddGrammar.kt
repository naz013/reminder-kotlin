package com.github.naz013.logic.quickadd.grammar

import com.github.naz013.logic.quickadd.ParsedDate
import com.github.naz013.logic.quickadd.QuickAddGrammar
import com.github.naz013.logic.quickadd.QuickAddMatch
import com.github.naz013.logic.quickadd.RecurrenceSignal
import com.github.naz013.logic.quickadd.toAppWeekday
import com.github.naz013.logic.quickadd.vocabulary.DateOrder
import com.github.naz013.logic.quickadd.vocabulary.QuickAddVocabulary
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.format.TextStyle
import java.util.Locale

/** A single [QuickAddGrammar] implementation shared by every Tier 1 language. The structural
 * pattern (every/at/on + a unit word) is identical across en/es/fr/de/pt/it - only the words and
 * day/month order differ, so those are the only things [vocabulary] needs to supply. Weekday and
 * month names are pulled from [locale]'s own JDK/ThreeTen display names rather than the
 * vocabulary, so they're always correct without manual translation.
 *
 * All matching is done against a locale-lowercased copy of the input rather than relying on
 * regex `IGNORE_CASE` - Java's `CASE_INSENSITIVE` alone only case-folds ASCII, which would miss
 * capitalized accented letters (e.g. german capitalizes weekday nouns). Lowercasing up front is
 * length-preserving for every Tier 1 language, so match ranges still index correctly into the
 * original, unmodified input the caller sees. */
class KeywordQuickAddGrammar(
  private val vocabulary: QuickAddVocabulary,
  private val locale: Locale,
) : QuickAddGrammar {

  override val languageTag: String get() = vocabulary.languageTag

  private val weekdaysByName: Map<String, Int> = DayOfWeek.entries.flatMap { day ->
    listOf(
      day.getDisplayName(TextStyle.FULL, locale).lowercase(locale) to day.toAppWeekday(),
      day.getDisplayName(TextStyle.SHORT, locale).lowercase(locale) to day.toAppWeekday(),
    )
  }.toMap()

  private val monthsByName: Map<String, Int> = Month.entries.flatMap { month ->
    listOf(
      month.getDisplayName(TextStyle.FULL, locale).lowercase(locale) to month.value,
      month.getDisplayName(TextStyle.SHORT, locale).lowercase(locale) to month.value,
    )
  }.toMap()

  private val weekdayAlternation = alternation(weekdaysByName.keys)
  private val monthAlternation = alternation(monthsByName.keys)

  override fun extractRecurrence(input: String): QuickAddMatch<RecurrenceSignal>? {
    val text = input.lowercase(locale)
    return matchEveryWeekday(text)
      ?: matchEveryWorkday(text)
      ?: dailySignal(text)
      ?: weeklySignal(text)
      ?: monthlySignal(text)
      ?: yearlySignal(text)
      ?: matchEveryOrdinalDay(text)
      ?: dailyAdverbSignal(text)
      ?: weeklyAdverbSignal(text)
      ?: monthlyAdverbSignal(text)
      ?: yearlyAdverbSignal(text)
  }

  override fun extractTime(input: String): QuickAddMatch<LocalTime>? {
    val text = input.lowercase(locale)
    return matchAdverb(text, vocabulary.noonWords)?.let { QuickAddMatch(LocalTime.NOON, it.range) }
      ?: matchAdverb(text, vocabulary.midnightWords)?.let { QuickAddMatch(LocalTime.MIDNIGHT, it.range) }
      ?: matchClockTime(text)
  }

  override fun extractDate(input: String): QuickAddMatch<ParsedDate>? {
    val text = input.lowercase(locale)
    return matchAdverb(text, vocabulary.todayWords)?.let { QuickAddMatch(ParsedDate.Today, it.range) }
      ?: matchAdverb(text, vocabulary.tomorrowWords)?.let { QuickAddMatch(ParsedDate.Tomorrow, it.range) }
      ?: matchWeekdayDate(text)
      ?: matchAbsoluteDate(text)
  }

  override fun cleanTitle(rawTitle: String): String {
    var result = rawTitle.replace(WHITESPACE_RUN, " ").trim()
    for (word in vocabulary.connectorWords) {
      val escaped = Regex.escape(word)
      result = Regex("(?i)^$escaped\\s+").replace(result, "")
      result = Regex("(?i)\\s+$escaped$").replace(result, "")
    }
    return result.trim().trim(',', '.', ';', ':', '-')
  }

  // --- recurrence signal wrappers (thin adapters so extractRecurrence stays a single elvis chain) ---

  private fun dailySignal(text: String): QuickAddMatch<RecurrenceSignal>? =
    matchEveryUnit(text, vocabulary.dailyUnitWords)?.let { QuickAddMatch(RecurrenceSignal.Daily(it.value), it.range) }

  private fun weeklySignal(text: String): QuickAddMatch<RecurrenceSignal>? =
    matchEveryUnit(text, vocabulary.weeklyUnitWords)
      ?.let { QuickAddMatch(RecurrenceSignal.Weekly(emptyList(), it.value), it.range) }

  private fun monthlySignal(text: String): QuickAddMatch<RecurrenceSignal>? =
    matchEveryUnit(text, vocabulary.monthlyUnitWords)
      ?.let { QuickAddMatch(RecurrenceSignal.Monthly(dayOfMonth = null, interval = it.value), it.range) }

  private fun yearlySignal(text: String): QuickAddMatch<RecurrenceSignal>? =
    matchEveryUnit(text, vocabulary.yearlyUnitWords)?.let { QuickAddMatch(RecurrenceSignal.Yearly(it.value), it.range) }

  private fun dailyAdverbSignal(text: String): QuickAddMatch<RecurrenceSignal>? =
    matchAdverb(text, vocabulary.dailyAdverbs)?.let { QuickAddMatch(RecurrenceSignal.Daily(), it.range) }

  private fun weeklyAdverbSignal(text: String): QuickAddMatch<RecurrenceSignal>? =
    matchAdverb(text, vocabulary.weeklyAdverbs)?.let { QuickAddMatch(RecurrenceSignal.Weekly(emptyList()), it.range) }

  private fun monthlyAdverbSignal(text: String): QuickAddMatch<RecurrenceSignal>? =
    matchAdverb(text, vocabulary.monthlyAdverbs)?.let {
      QuickAddMatch(RecurrenceSignal.Monthly(dayOfMonth = null), it.range)
    }

  private fun yearlyAdverbSignal(text: String): QuickAddMatch<RecurrenceSignal>? =
    matchAdverb(text, vocabulary.yearlyAdverbs)?.let { QuickAddMatch(RecurrenceSignal.Yearly(), it.range) }

  // --- recurrence ---

  private fun matchEveryWeekday(text: String): QuickAddMatch<RecurrenceSignal>? {
    if (vocabulary.everyWords.isEmpty() || weekdaysByName.isEmpty()) return null
    val pattern = withBoundaries("(?:${alternation(vocabulary.everyWords)})\\s+(?<weekday>$weekdayAlternation)")
    return Regex(pattern).find(text)?.let { match ->
      weekdaysByName[match.groups["weekday"]?.value]?.let { weekday ->
        QuickAddMatch(RecurrenceSignal.Weekly(listOf(weekday)), match.range)
      }
    }
  }

  private fun matchEveryWorkday(text: String): QuickAddMatch<RecurrenceSignal>? {
    if (vocabulary.everyWords.isEmpty() || vocabulary.workdayWords.isEmpty()) return null
    val pattern = withBoundaries(
      "(?:${alternation(vocabulary.everyWords)})\\s+(?:${alternation(vocabulary.workdayWords)})"
    )
    return Regex(pattern).find(text)?.let { QuickAddMatch(RecurrenceSignal.Weekly(listOf(1, 2, 3, 4, 5)), it.range) }
  }

  private fun matchEveryUnit(text: String, unitWords: List<String>): QuickAddMatch<Long>? {
    if (unitWords.isEmpty() || vocabulary.everyWords.isEmpty()) return null
    val pattern =
      withBoundaries(
        "(?:${alternation(vocabulary.everyWords)})\\s+(?:(?<interval>\\d{1,2})\\s+)?(?:${alternation(unitWords)})",
      )
    return Regex(pattern).find(text)?.let { match ->
      QuickAddMatch(match.groups["interval"]?.value?.toLongOrNull() ?: 1L, match.range)
    }
  }

  private fun matchEveryOrdinalDay(text: String): QuickAddMatch<RecurrenceSignal>? {
    if (vocabulary.everyWords.isEmpty()) return null
    val suffix = if (vocabulary.ordinalSuffixRegex.isEmpty()) "" else "(?:${vocabulary.ordinalSuffixRegex})?"
    val pattern = withBoundaries("(?:${alternation(vocabulary.everyWords)})\\s+(?<day>\\d{1,2})$suffix")
    return Regex(pattern).find(text)?.let { match ->
      match.groups["day"]?.value?.toIntOrNull()?.takeIf { it in 1..31 }?.let { day ->
        QuickAddMatch(RecurrenceSignal.Monthly(dayOfMonth = day), match.range)
      }
    }
  }

  private fun matchAdverb(text: String, words: List<String>): QuickAddMatch<Unit>? {
    if (words.isEmpty()) return null
    return Regex(withBoundaries(alternation(words))).find(text)?.let { QuickAddMatch(Unit, it.range) }
  }

  // --- time ---

  private fun matchClockTime(text: String): QuickAddMatch<LocalTime>? {
    val meridiemWords = vocabulary.amWords + vocabulary.pmWords
    val meridiemGroup = if (meridiemWords.isNotEmpty()) "(?:\\s*(?<meridiem>${alternation(meridiemWords)}))?" else ""
    val unitGroup = if (vocabulary.timeUnitWords.isNotEmpty()) {
      "(?:\\s+(?:${alternation(vocabulary.timeUnitWords)}))?"
    } else {
      ""
    }
    val timeCore = "(?<hour>\\d{1,2})(?::(?<minute>\\d{2}))?"

    val prefixedMatch = vocabulary.atWords.takeIf { it.isNotEmpty() }?.let { atWords ->
      val pattern = withBoundaries("(?:${alternation(atWords)})\\s+$timeCore$meridiemGroup$unitGroup")
      Regex(pattern).find(text)
    }
    val bareMatch = if (prefixedMatch == null && meridiemWords.isNotEmpty()) {
      Regex(withBoundaries("$timeCore\\s*(?<meridiem>${alternation(meridiemWords)})")).find(text)
    } else {
      null
    }

    return (prefixedMatch ?: bareMatch)?.let { match ->
      toLocalTime(match, meridiemWords)?.let { QuickAddMatch(it, match.range) }
    }
  }

  private fun toLocalTime(match: MatchResult, meridiemWords: List<String>): LocalTime? {
    val hourRaw = match.groups["hour"]?.value?.toIntOrNull() ?: return null
    val minute = match.groups["minute"]?.value?.toIntOrNull() ?: 0
    // A named group that isn't declared anywhere in the pattern (as opposed to one that's simply
    // unmatched) throws on lookup - guard for languages with no am/pm words (e.g. German).
    val meridiem = if (meridiemWords.isNotEmpty()) match.groups["meridiem"]?.value else null
    val isPm = meridiem != null && vocabulary.pmWords.any { it == meridiem }
    val isAm = meridiem != null && vocabulary.amWords.any { it == meridiem }
    val hour = when {
      isPm && hourRaw in 1..11 -> hourRaw + 12
      isAm && hourRaw == 12 -> 0
      else -> hourRaw
    }
    return if (hour in 0..23 && minute in 0..59) LocalTime.of(hour, minute) else null
  }

  // --- date ---

  private fun matchWeekdayDate(text: String): QuickAddMatch<ParsedDate>? {
    if (weekdaysByName.isEmpty()) return null
    val onGroup = if (vocabulary.onWords.isNotEmpty()) "(?:(?:${alternation(vocabulary.onWords)})\\s+)?" else ""
    val nextGroup = if (vocabulary.nextWords.isNotEmpty()) {
      "(?:(?<next>${alternation(vocabulary.nextWords)})\\s+)?"
    } else {
      ""
    }
    val pattern = withBoundaries("$onGroup$nextGroup(?<weekday>$weekdayAlternation)")
    return Regex(pattern).find(text)?.let { match ->
      weekdaysByName[match.groups["weekday"]?.value]?.let { weekday ->
        val forceNextWeek = vocabulary.nextWords.isNotEmpty() && match.groups["next"] != null
        QuickAddMatch(ParsedDate.NextWeekday(weekday, forceNextWeek), match.range)
      }
    }
  }

  private fun matchAbsoluteDate(text: String): QuickAddMatch<ParsedDate>? {
    if (monthsByName.isEmpty()) return null
    return Regex(buildAbsoluteDatePattern()).find(text)?.let { match ->
      toAbsoluteDate(match)?.let { QuickAddMatch(it, match.range) }
    }
  }

  private fun buildAbsoluteDatePattern(): String {
    val yearGroup = "(?:[,\\s]+(?<year>\\d{4}))?"
    return when (vocabulary.dateOrder) {
      DateOrder.MONTH_DAY -> withBoundaries("(?<month>$monthAlternation)\\s+(?<day>\\d{1,2})$yearGroup")
      DateOrder.DAY_MONTH -> {
        val connector = if (vocabulary.dayMonthConnectors.isNotEmpty()) {
          "(?:\\s+(?:${alternation(vocabulary.dayMonthConnectors)}))?"
        } else {
          ""
        }
        withBoundaries("(?<day>\\d{1,2})\\.?$connector\\s+(?<month>$monthAlternation)$yearGroup")
      }
    }
  }

  private fun toAbsoluteDate(match: MatchResult): ParsedDate.Absolute? {
    val day = match.groups["day"]?.value?.toIntOrNull()?.takeIf { it in 1..31 }
    val month = match.groups["month"]?.value?.let { monthsByName[it] }
    val year = match.groups["year"]?.value?.toIntOrNull()
    return if (day != null && month != null) ParsedDate.Absolute(month, day, year) else null
  }

  companion object {
    private val WHITESPACE_RUN = Regex("\\s{2,}")

    private fun alternation(words: Collection<String>): String =
      words.filter { it.isNotBlank() }
        .distinct()
        .sortedByDescending { it.length }
        .joinToString("|") { Regex.escape(it) }

    /** Unicode-aware word boundary: plain `\b` only understands ASCII word characters unless the
     * `UNICODE_CHARACTER_CLASS` flag is set, which breaks matching around accented letters
     * (é/ñ/ã/ç/...) common in es/fr/pt/it. */
    private fun withBoundaries(pattern: String): String = "(?<![\\p{L}\\p{N}])(?:$pattern)(?![\\p{L}\\p{N}])"
  }
}
