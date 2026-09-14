package com.github.naz013.logic.quickadd

import com.github.naz013.logic.quickadd.grammar.KeywordQuickAddGrammar
import com.github.naz013.logic.quickadd.vocabulary.QuickAddVocabularies
import java.util.Locale

/** Default grammar set for [QuickAddParser]. Covers every app language whose grammar fits the
 * shared [KeywordQuickAddGrammar] engine (space-tokenized text, prefix markers like "every"/"at")
 * - a language with no grammar here simply isn't recognized, and quick-add falls back to the full
 * wizard. Chinese, Japanese, Korean, and Thai aren't included: they have no whitespace word
 * boundaries at all, which the engine's regex-based matching fundamentally can't segment. Hindi
 * isn't included either: its core time/date markers ("को" for "on", "बजे" for "at") are
 * postpositions that follow the word rather than precede it, a grammar shape this prefix-only
 * engine can't express - all five need real engine work, not just a vocabulary table. */
object QuickAddGrammars {
  fun supported(): List<QuickAddGrammar> =
    listOf(
      KeywordQuickAddGrammar(QuickAddVocabularies.english, Locale.ENGLISH),
      KeywordQuickAddGrammar(QuickAddVocabularies.spanish, Locale.forLanguageTag("es")),
      KeywordQuickAddGrammar(QuickAddVocabularies.french, Locale.FRENCH),
      KeywordQuickAddGrammar(QuickAddVocabularies.german, Locale.GERMAN),
      KeywordQuickAddGrammar(QuickAddVocabularies.portuguese, Locale.forLanguageTag("pt")),
      KeywordQuickAddGrammar(QuickAddVocabularies.italian, Locale.ITALIAN),
      KeywordQuickAddGrammar(QuickAddVocabularies.dutch, Locale.forLanguageTag("nl")),
      KeywordQuickAddGrammar(QuickAddVocabularies.swedish, Locale.forLanguageTag("sv")),
      KeywordQuickAddGrammar(QuickAddVocabularies.danish, Locale.forLanguageTag("da")),
      KeywordQuickAddGrammar(QuickAddVocabularies.norwegian, Locale.forLanguageTag("nb")),
      KeywordQuickAddGrammar(QuickAddVocabularies.polish, Locale.forLanguageTag("pl")),
      KeywordQuickAddGrammar(QuickAddVocabularies.czech, Locale.forLanguageTag("cs")),
      KeywordQuickAddGrammar(QuickAddVocabularies.romanian, Locale.forLanguageTag("ro")),
      KeywordQuickAddGrammar(QuickAddVocabularies.ukrainian, Locale.forLanguageTag("uk")),
      KeywordQuickAddGrammar(QuickAddVocabularies.russian, Locale.forLanguageTag("ru")),
      KeywordQuickAddGrammar(QuickAddVocabularies.bulgarian, Locale.forLanguageTag("bg")),
      KeywordQuickAddGrammar(QuickAddVocabularies.indonesian, Locale.forLanguageTag("id")),
      KeywordQuickAddGrammar(QuickAddVocabularies.vietnamese, Locale.forLanguageTag("vi")),
      KeywordQuickAddGrammar(QuickAddVocabularies.turkish, Locale.forLanguageTag("tr")),
      KeywordQuickAddGrammar(QuickAddVocabularies.arabic, Locale.forLanguageTag("ar")),
    )
}
