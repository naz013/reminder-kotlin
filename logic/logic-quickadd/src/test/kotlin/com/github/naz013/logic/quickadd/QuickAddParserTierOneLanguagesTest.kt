package com.github.naz013.logic.quickadd

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

/** Spot checks (not exhaustive linguistic coverage - see QuickAddVocabulary's KDoc) proving the
 * shared KeywordQuickAddGrammar engine is correctly wired for each Tier 1 language via its
 * vocabulary table, mirroring the card's canonical "pay rent every 1st at 9am" example plus a
 * simple "tomorrow" phrase per language. */
class QuickAddParserTierOneLanguagesTest {

  private val parser = QuickAddParser()

  // Monday 2025-01-06, 08:00, same anchor as QuickAddParserTest for cross-language comparison.
  private val now = LocalDateTime.of(2025, 1, 6, 8, 0)

  @Test
  fun `spanish - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("pagar el alquiler cada 1 a las 9", "es", now) as QuickAddResult.Parsed

    assertEquals("pagar el alquiler", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `spanish - tomorrow`() {
    val result = parser.parse("comprar pan manana", "es", now) as QuickAddResult.Parsed

    assertEquals("comprar pan", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `french - weekly recurrence with time`() {
    val result = parser.parse("arroser les plantes chaque semaine à 9 heures", "fr", now) as QuickAddResult.Parsed

    assertEquals("arroser les plantes", result.title)
    assertEquals(9, result.startDateTime.hour)
    // weekdays is a 7-element bitmask (index 0=Sunday..6=Saturday), not a list of selected days.
    val expectedWeekdays = List(7) { if (it == now.dayOfWeek.toAppWeekday()) 1 else 0 }
    val expected = RecurrenceRule.Weekly(weekdays = expectedWeekdays, repeatInterval = 1)
    assertEquals(expected, result.recurrence)
  }

  @Test
  fun `french - tomorrow`() {
    val result = parser.parse("appeler maman demain à 15 heures", "fr", now) as QuickAddResult.Parsed

    assertEquals("appeler maman", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 7, 15, 0), result.startDateTime)
  }

  @Test
  fun `german - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("miete bezahlen jeden 1. um 9 uhr", "de", now) as QuickAddResult.Parsed

    assertEquals("miete bezahlen", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `german - tomorrow`() {
    val result = parser.parse("mama anrufen morgen um 15 uhr", "de", now) as QuickAddResult.Parsed

    assertEquals("mama anrufen", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 7, 15, 0), result.startDateTime)
  }

  @Test
  fun `portuguese - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("pagar o aluguel cada 1 às 9", "pt", now) as QuickAddResult.Parsed

    assertEquals("pagar o aluguel", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `portuguese - tomorrow`() {
    val result = parser.parse("comprar pao amanha", "pt", now) as QuickAddResult.Parsed

    assertEquals("comprar pao", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `italian - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("pagare l'affitto ogni 1 alle 9", "it", now) as QuickAddResult.Parsed

    assertEquals("pagare l'affitto", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `italian - tomorrow`() {
    val result = parser.parse("chiamare la mamma domani alle 15", "it", now) as QuickAddResult.Parsed

    assertEquals("chiamare la mamma", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 7, 15, 0), result.startDateTime)
  }
}
