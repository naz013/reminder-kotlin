package com.github.naz013.domain.reminder.v2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime

class RecurrenceRuleTest {

  @Test
  fun `untilOrNull returns the end date for Daily, Weekly, Monthly, RelativeMonthly, and Yearly`() {
    val until = LocalDateTime.of(2026, 12, 31, 0, 0)

    assertEquals(until, RecurrenceRule.Daily(until = until).untilOrNull())
    assertEquals(until, RecurrenceRule.Weekly(weekdays = listOf(1), until = until).untilOrNull())
    assertEquals(until, RecurrenceRule.Monthly(dayOfMonth = 1, until = until).untilOrNull())
    assertEquals(until, RecurrenceRule.RelativeMonthly(weekday = 1, ordinal = 1, until = until).untilOrNull())
    assertEquals(until, RecurrenceRule.Yearly(dayOfMonth = 1, monthOfYear = 0, until = until).untilOrNull())
  }

  @Test
  fun `untilOrNull is null when the calendar-unit variants don't carry an end date`() {
    assertNull(RecurrenceRule.Daily(until = null).untilOrNull())
    assertNull(RecurrenceRule.Weekly(weekdays = listOf(1), until = null).untilOrNull())
  }

  @Test
  fun `untilOrNull is null for variants that don't model an end date at all`() {
    assertNull(RecurrenceRule.Once.untilOrNull())
    assertNull(RecurrenceRule.Countdown(after = 60_000L).untilOrNull())
    assertNull(RecurrenceRule.LocationEnter.untilOrNull())
    assertNull(RecurrenceRule.LocationExit.untilOrNull())
    assertNull(RecurrenceRule.ICalendar(rrule = "RRULE:FREQ=DAILY").untilOrNull())
  }
}
