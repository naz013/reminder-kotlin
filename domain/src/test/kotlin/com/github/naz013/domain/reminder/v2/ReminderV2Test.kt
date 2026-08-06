package com.github.naz013.domain.reminder.v2

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderV2Test {

  private fun reminderWith(recurrence: RecurrenceRule, eventCount: Long = 0): ReminderV2 =
    ReminderV2(recurrence = recurrence, schedule = ReminderSchedule(LocalDateTime.now()), eventCount = eventCount)

  @Test
  fun `isLimited is false when Daily has no repeat limit set`() {
    val reminder = reminderWith(RecurrenceRule.Daily(repeatLimit = -1))

    assertFalse(reminder.isLimited())
  }

  @Test
  fun `isLimited is true when Daily has a positive repeat limit`() {
    val reminder = reminderWith(RecurrenceRule.Daily(repeatLimit = 3))

    assertTrue(reminder.isLimited())
  }

  @Test
  fun `isLimited is false when repeatLimit is exactly zero, same as unlimited`() {
    val reminder = reminderWith(RecurrenceRule.Daily(repeatLimit = 0))

    assertFalse(reminder.isLimited())
  }

  @Test
  fun `isLimitExceed is true once a limited Weekly reminder uses up its limit`() {
    val reminder = reminderWith(RecurrenceRule.Weekly(weekdays = listOf(1, 3, 5), repeatLimit = 3), eventCount = 3)

    assertTrue(reminder.isLimitExceed())
  }

  @Test
  fun `isLimitExceed is false while a limited Monthly reminder still has events remaining`() {
    val reminder = reminderWith(RecurrenceRule.Monthly(dayOfMonth = 15, repeatLimit = 3), eventCount = 2)

    assertFalse(reminder.isLimitExceed())
  }

  @Test
  fun `isLimitExceed is true once a limited Yearly reminder uses up its limit`() {
    val reminder = reminderWith(RecurrenceRule.Yearly(dayOfMonth = 1, monthOfYear = 0, repeatLimit = 1), eventCount = 1)

    assertTrue(reminder.isLimitExceed())
  }

  @Test
  fun `isLimitExceed is true once a limited Countdown reminder uses up its limit`() {
    val reminder = reminderWith(RecurrenceRule.Countdown(after = 60_000L, repeatLimit = 2), eventCount = 2)

    assertTrue(reminder.isLimitExceed())
  }

  @Test
  fun `isLimitExceed is true once a limited RelativeMonthly reminder uses up its limit`() {
    val reminder = reminderWith(RecurrenceRule.RelativeMonthly(weekday = 2, ordinal = 1, repeatLimit = 1), eventCount = 1)

    assertTrue(reminder.isLimitExceed())
  }

  @Test
  fun `isLimitExceed is false when not limited regardless of eventCount`() {
    val reminder = reminderWith(RecurrenceRule.Weekly(weekdays = listOf(1), repeatLimit = -1), eventCount = 100)

    assertFalse(reminder.isLimitExceed())
  }

  @Test
  fun `isLimited is false for Once, which carries no repeat limit at all`() {
    val reminder = reminderWith(RecurrenceRule.Once)

    assertFalse(reminder.isLimited())
  }

  @Test
  fun `isLimited is false for LocationEnter, which carries no repeat limit at all`() {
    val reminder = reminderWith(RecurrenceRule.LocationEnter)

    assertFalse(reminder.isLimited())
  }

  @Test
  fun `isLimited is false for ICalendar, which carries no repeat limit at all`() {
    val reminder = reminderWith(RecurrenceRule.ICalendar(rrule = "RRULE:FREQ=DAILY"))

    assertFalse(reminder.isLimited())
  }
}
