package com.github.naz013.logic.quickadd

import com.github.naz013.logic.quickadd.grammar.KeywordQuickAddGrammar
import com.github.naz013.logic.quickadd.vocabulary.QuickAddVocabularies
import java.util.Locale

/** Default grammar set for [QuickAddParser]. Tier 1 launch languages only (REM-1221) - a language
 * with no grammar here simply isn't recognized, and quick-add falls back to the full wizard. */
object QuickAddGrammars {
  fun tier1(): List<QuickAddGrammar> = listOf(
    KeywordQuickAddGrammar(QuickAddVocabularies.english, Locale.ENGLISH),
    KeywordQuickAddGrammar(QuickAddVocabularies.spanish, Locale.forLanguageTag("es")),
    KeywordQuickAddGrammar(QuickAddVocabularies.french, Locale.FRENCH),
    KeywordQuickAddGrammar(QuickAddVocabularies.german, Locale.GERMAN),
    KeywordQuickAddGrammar(QuickAddVocabularies.portuguese, Locale.forLanguageTag("pt")),
    KeywordQuickAddGrammar(QuickAddVocabularies.italian, Locale.ITALIAN),
  )
}
