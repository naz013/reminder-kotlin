package com.github.naz013.feature.settings.debug

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class RecurrenceDateExpansionTest {

  private val windowStart = LocalDate.of(2026, 7, 1)
  private val windowEnd = LocalDate.of(2026, 7, 31)

  @Test
  fun `Once yields a single date when the anchor falls inside the window`() {
    val anchor = LocalDate.of(2026, 7, 10)

    val dates = expandRecurrenceDates(RecurrenceRule.Once, anchor, windowStart, windowEnd)

    assertEquals(listOf(anchor), dates)
  }

  @Test
  fun `Once yields nothing when the anchor falls outside the window`() {
    val anchor = LocalDate.of(2026, 8, 1)

    val dates = expandRecurrenceDates(RecurrenceRule.Once, anchor, windowStart, windowEnd)

    assertEquals(emptyList<LocalDate>(), dates)
  }

  @Test
  fun `Daily fills every day from the anchor through the window end`() {
    val anchor = LocalDate.of(2026, 7, 28)

    val dates = expandRecurrenceDates(RecurrenceRule.Daily(repeatInterval = 1), anchor, windowStart, windowEnd)

    assertEquals(listOf(28, 29, 30, 31).map { LocalDate.of(2026, 7, it) }, dates)
  }

  @Test
  fun `Daily clamps its start to the window when the anchor is before it`() {
    val anchor = LocalDate.of(2026, 6, 1)

    val dates = expandRecurrenceDates(RecurrenceRule.Daily(repeatInterval = 1), anchor, windowStart, windowEnd)

    assertEquals(31, dates.size)
    assertEquals(windowStart, dates.first())
    assertEquals(windowEnd, dates.last())
  }

  @Test
  fun `Weekly only lands on the configured weekday`() {
    // 2026-07-01 is a Wednesday; weekday index 3 in the app's 0=Sunday..6=Saturday convention.
    val anchor = LocalDate.of(2026, 7, 1)
    val weekdays = List(7) { if (it == 3) 1 else 0 }

    val dates = expandRecurrenceDates(RecurrenceRule.Weekly(weekdays = weekdays), anchor, windowStart, windowEnd)

    assertEquals(listOf(1, 8, 15, 22, 29).map { LocalDate.of(2026, 7, it) }, dates)
    dates.forEach { assertEquals(3, it.dayOfWeek.value % 7) }
  }

  @Test
  fun `Monthly repeats on the same day of month, clamped to shorter months`() {
    val anchor = LocalDate.of(2026, 1, 31)
    val threeMonthWindowEnd = LocalDate.of(2026, 3, 15)

    val dates =
      expandRecurrenceDates(RecurrenceRule.Monthly(dayOfMonth = 31), anchor, LocalDate.of(2026, 1, 1), threeMonthWindowEnd)

    // 2026 is not a leap year, so February clamps 31 down to its actual last day (28); March 31
    // falls after the window end (March 15) so it's excluded entirely.
    assertEquals(listOf(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28)), dates)
  }

  @Test
  fun `RelativeMonthly finds the nth weekday of each month`() {
    // Second Tuesday of July 2026 is the 14th.
    val anchor = LocalDate.of(2026, 7, 1)
    val tuesday = 2

    val dates = expandRecurrenceDates(RecurrenceRule.RelativeMonthly(weekday = tuesday, ordinal = 2), anchor, windowStart, windowEnd)

    assertEquals(listOf(LocalDate.of(2026, 7, 14)), dates)
  }

  @Test
  fun `RelativeMonthly returns nothing for an ordinal that does not exist in the month`() {
    // July 2026 has only four Sundays (5, 12, 19, 26) - no fifth one.
    val anchor = LocalDate.of(2026, 7, 1)
    val sunday = 0

    val dates = expandRecurrenceDates(RecurrenceRule.RelativeMonthly(weekday = sunday, ordinal = 5), anchor, windowStart, windowEnd)

    assertEquals(emptyList<LocalDate>(), dates)
  }

  @Test
  fun `Yearly only lands when the month and day fall inside the window`() {
    val anchor = LocalDate.of(2020, 7, 15)

    val dates = expandRecurrenceDates(RecurrenceRule.Yearly(dayOfMonth = 15, monthOfYear = 6), anchor, windowStart, windowEnd)

    assertEquals(listOf(LocalDate.of(2026, 7, 15)), dates)
  }

  @Test
  fun `Yearly yields nothing when the month falls outside the window`() {
    val anchor = LocalDate.of(2020, 12, 25)

    val dates = expandRecurrenceDates(RecurrenceRule.Yearly(dayOfMonth = 25, monthOfYear = 11), anchor, windowStart, windowEnd)

    assertEquals(emptyList<LocalDate>(), dates)
  }

  @Test
  fun `nthWeekdayOfMonth returns the correct date for a middle-of-month ordinal`() {
    val result = nthWeekdayOfMonth(LocalDate.of(2026, 7, 1), weekday = 2, ordinal = 2)

    assertEquals(LocalDate.of(2026, 7, 14), result)
  }

  @Test
  fun `nthWeekdayOfMonth returns null when the ordinal spills into the next month`() {
    // July 2026 has only four Sundays.
    val result = nthWeekdayOfMonth(LocalDate.of(2026, 7, 1), weekday = 0, ordinal = 5)

    assertEquals(null, result)
  }
}
