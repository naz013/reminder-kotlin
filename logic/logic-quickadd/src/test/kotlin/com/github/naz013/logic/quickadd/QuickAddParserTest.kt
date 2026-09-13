package com.github.naz013.logic.quickadd

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class QuickAddParserTest {

  private val parser = QuickAddParser()

  // Monday 2025-01-06, 08:00 - a fixed "now" so relative phrases resolve deterministically.
  private val now = LocalDateTime.of(2025, 1, 6, 8, 0)

  @Test
  fun `parses monthly recurrence with ordinal day and time`() {
    val result = parser.parse("pay rent every 1st at 9am", "en", now) as QuickAddResult.Parsed

    assertEquals("pay rent", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), result.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `parses daily recurrence with adverb`() {
    val result = parser.parse("take pills daily at 8:30am", "en", now) as QuickAddResult.Parsed

    assertEquals("take pills", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 6, 8, 30), result.startDateTime)
    assertEquals(RecurrenceRule.Daily(repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `parses weekly recurrence anchored to a specific weekday`() {
    val result = parser.parse("gym every monday at 6pm", "en", now) as QuickAddResult.Parsed

    assertEquals("gym", result.title)
    // weekdays is a 7-element bitmask (index 0=Sunday..6=Saturday) - see ByWeekdaysDecomposer/
    // WeekDaysProtocol - not a list of selected day indices; index 1 (Monday) is set here.
    assertEquals(RecurrenceRule.Weekly(weekdays = listOf(0, 1, 0, 0, 0, 0, 0), repeatInterval = 1), result.recurrence)
    assertEquals(18, result.startDateTime.hour)
  }

  @Test
  fun `parses every N weeks interval`() {
    val result = parser.parse("water plants every 2 weeks at 9am", "en", now) as QuickAddResult.Parsed

    val expectedWeekdays = List(7) { if (it == now.dayOfWeek.toAppWeekday()) 1 else 0 }
    val expected = RecurrenceRule.Weekly(weekdays = expectedWeekdays, repeatInterval = 2)
    assertEquals(expected, result.recurrence)
  }

  @Test
  fun `parses yearly recurrence anchored to an explicit month and day`() {
    val result = parser.parse("renew passport every year on august 5", "en", now) as QuickAddResult.Parsed

    assertEquals("renew passport", result.title)
    assertEquals(RecurrenceRule.Yearly(dayOfMonth = 5, monthOfYear = 8, repeatInterval = 1), result.recurrence)
  }

  @Test
  fun `one-off reminder with explicit weekday and time defaults recurrence to Once`() {
    val result = parser.parse("call mom on monday at 3pm", "en", now) as QuickAddResult.Parsed

    assertEquals("call mom", result.title)
    assertEquals(RecurrenceRule.Once, result.recurrence)
    // "now" is already Monday - a plain "on monday" (no "next") should resolve to today.
    assertEquals(LocalDateTime.of(2025, 1, 6, 15, 0), result.startDateTime)
  }

  @Test
  fun `next weekday skips the current week even if today matches`() {
    val result = parser.parse("call mom next monday at 3pm", "en", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 13, 15, 0), result.startDateTime)
  }

  @Test
  fun `tomorrow resolves relative to now`() {
    val result = parser.parse("dentist tomorrow at 10am", "en", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 10, 0), result.startDateTime)
  }

  @Test
  fun `a one-off time that already passed today rolls forward to tomorrow`() {
    val laterNow = LocalDateTime.of(2025, 1, 6, 15, 0)
    val result = parser.parse("call John at 9am", "en", laterNow) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `a recurring anchor already past today is not rolled forward`() {
    val laterNow = LocalDateTime.of(2025, 1, 6, 15, 0)
    val result = parser.parse("take pills every day at 9am", "en", laterNow) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 6, 9, 0), result.startDateTime)
  }

  @Test
  fun `defaults to 9am when no time is given`() {
    val result = parser.parse("call mom tomorrow", "en", now) as QuickAddResult.Parsed

    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `plain text with no recognizable date time or recurrence is partially parsed`() {
    val result = parser.parse("buy milk", "en", now) as QuickAddResult.PartiallyParsed

    assertEquals("buy milk", result.title)
    assertNull(result.startDateTime)
    assertNull(result.recurrence)
    assertTrue(result.matchedRanges.isEmpty())
  }

  @Test
  fun `schedule with no leftover title text is partially parsed`() {
    val result = parser.parse("tomorrow at 9am", "en", now) as QuickAddResult.PartiallyParsed

    assertEquals("", result.title)
    assertEquals(LocalDateTime.of(2025, 1, 7, 9, 0), result.startDateTime)
  }

  @Test
  fun `unsupported language returns Empty`() {
    assertEquals(QuickAddResult.Empty, parser.parse("every day at 9am", "ja", now))
  }

  @Test
  fun `blank input returns Empty`() {
    assertEquals(QuickAddResult.Empty, parser.parse("   ", "en", now))
    assertEquals(QuickAddResult.Empty, parser.parse("", "en", now))
  }

  @Test
  fun `noon and midnight resolve to fixed times`() {
    val noon = parser.parse("lunch with Sara tomorrow at noon", "en", now) as QuickAddResult.Parsed
    val midnight = parser.parse("release notes tomorrow at midnight", "en", now) as QuickAddResult.Parsed

    assertEquals(12, noon.startDateTime.hour)
    assertEquals(0, midnight.startDateTime.hour)
  }

  @Test
  fun `12am and 12pm are resolved correctly`() {
    val am = parser.parse("backup at 12am", "en", now) as QuickAddResult.Parsed
    val pm = parser.parse("backup at 12pm", "en", now) as QuickAddResult.Parsed

    assertEquals(0, am.startDateTime.hour)
    assertEquals(12, pm.startDateTime.hour)
  }
}
