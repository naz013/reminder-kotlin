package com.github.naz013.logic.quickadd

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

/** Spot checks for the 14 languages added beyond Tier 1 (see QuickAddVocabulary's KDoc for the
 * "not exhaustive" scope note) - one canonical "pay rent every 1st at 9" example plus a simple
 * "tomorrow" phrase per language, mirroring [QuickAddParserTierOneLanguagesTest]. */
class QuickAddParserExpandedLanguagesTest {

  private val parser = QuickAddParser()

  // Monday 2025-01-06, 08:00, same anchor as the other parser test classes.
  private val now = LocalDateTime.of(2025, 1, 6, 8, 0)

  @Test
  fun `dutch - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("huur betalen elke 1e om 9 uur", "nl", now) as QuickAddResult.Parsed

    assertEquals("huur betalen", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `dutch - tomorrow`() {
    val result = parser.parse("brood kopen morgen", "nl", now) as QuickAddResult.Parsed

    assertEquals("brood kopen", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `swedish - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("betala hyra varje 1:a klockan 9", "sv", now) as QuickAddResult.Parsed

    assertEquals("betala hyra", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `swedish - tomorrow`() {
    val result = parser.parse("kopa brod imorgon", "sv", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `danish - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("betale husleje hver 1. klokken 9", "da", now) as QuickAddResult.Parsed

    assertEquals("betale husleje", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `danish - tomorrow`() {
    val result = parser.parse("kobe brod i morgen", "da", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `norwegian - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("betale husleie hver 1. klokken 9", "nb", now) as QuickAddResult.Parsed

    assertEquals("betale husleie", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `norwegian - tomorrow`() {
    val result = parser.parse("kjope brod i morgen", "nb", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `polish - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("zaplacic czynsz kazdego 1 o 9", "pl", now) as QuickAddResult.Parsed

    assertEquals("zaplacic czynsz", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `polish - tomorrow`() {
    val result = parser.parse("kupic chleb jutro", "pl", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `czech - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("zaplatit najem kazdeho 1 v 9", "cs", now) as QuickAddResult.Parsed

    assertEquals("zaplatit najem", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `czech - tomorrow`() {
    val result = parser.parse("koupit chleba zitra", "cs", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `romanian - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("plateste chiria in fiecare 1 la 9", "ro", now) as QuickAddResult.Parsed

    assertEquals("plateste chiria", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `romanian - tomorrow`() {
    val result = parser.parse("cumpara paine maine", "ro", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `ukrainian - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("заплатити оренду кожного 1 о 9", "uk", now) as QuickAddResult.Parsed

    assertEquals("заплатити оренду", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `ukrainian - tomorrow`() {
    val result = parser.parse("купити хліб завтра", "uk", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `russian - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("заплатить аренду каждого 1 в 9", "ru", now) as QuickAddResult.Parsed

    assertEquals("заплатить аренду", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `russian - tomorrow`() {
    val result = parser.parse("купить хлеб завтра", "ru", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `bulgarian - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("плати наем всеки 1 в 9", "bg", now) as QuickAddResult.Parsed

    assertEquals("плати наем", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `bulgarian - tomorrow`() {
    val result = parser.parse("купи хляб утре", "bg", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `indonesian - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("bayar sewa setiap 1 jam 9 pagi", "id", now) as QuickAddResult.Parsed

    assertEquals("bayar sewa", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `indonesian - tomorrow`() {
    val result = parser.parse("beli roti besok", "id", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `vietnamese - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("tra tien thue nha moi 1 luc 9 sang", "vi", now) as QuickAddResult.Parsed

    assertEquals("tra tien thue nha", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `vietnamese - tomorrow`() {
    val result = parser.parse("mua banh mi ngay mai", "vi", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `turkish - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("kira ode her 1 saat 9", "tr", now) as QuickAddResult.Parsed

    assertEquals("kira ode", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `turkish - tomorrow`() {
    val result = parser.parse("ekmek al yarin", "tr", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `arabic - monthly recurrence with ordinal day and time`() {
    val result = parser.parse("دفع الإيجار كل 1 الساعة 9", "ar", now) as QuickAddResult.Parsed

    assertEquals("دفع الإيجار", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `arabic - tomorrow`() {
    val result = parser.parse("شراء الخبز غدا", "ar", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }
}
