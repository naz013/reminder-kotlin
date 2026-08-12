package com.github.naz013.domain.reminder.migration

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderToReminderV2MapperTest {

  @Test
  fun `maps a plain one-time reminder to RecurrenceRule Once`() {
    val reminder = Reminder(type = Reminder.BY_DATE, startTime = GMT_TIME)

    val result = reminder.toReminderV2()

    assertEquals(RecurrenceRule.Once, result.recurrence)
    assertEquals(ReminderAction.None, result.action)
  }

  @Test
  fun `maps a repeating by-date reminder to RecurrenceRule Daily carrying the raw millis interval`() {
    val reminder = Reminder(
      type = Reminder.BY_DATE,
      repeatInterval = 21_600_000L, // every 6 hours - not a whole-day multiple
      repeatLimit = 5,
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(RecurrenceRule.Daily(repeatInterval = 21_600_000L, repeatLimit = 5), result.recurrence)
  }

  @Test
  fun `maps an app-launch reminder`() {
    val reminder = Reminder(
      type = Reminder.BY_DATE + Reminder.Action.APP,
      target = "com.example.app",
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(ReminderAction.App(target = "com.example.app"), result.action)
  }

  @Test
  fun `maps a repeating countdown timer to RecurrenceRule Countdown carrying the raw millis interval`() {
    val reminder = Reminder(
      type = Reminder.BY_TIME,
      after = 60_000L,
      repeatInterval = 7_200_000L, // every 2 hours
      repeatLimit = 3,
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(
      RecurrenceRule.Countdown(after = 60_000L, repeatInterval = 7_200_000L, repeatLimit = 3),
      result.recurrence
    )
  }

  @Test
  fun `maps a countdown reminder with a call action`() {
    val reminder = Reminder(
      type = Reminder.BY_TIME + Reminder.Action.CALL,
      after = 60_000L,
      target = "+123456789",
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(RecurrenceRule.Countdown(after = 60_000L), result.recurrence)
    assertEquals(ReminderAction.Call(target = "+123456789"), result.action)
  }

  @Test
  fun `maps a weekly reminder`() {
    val reminder = Reminder(
      type = Reminder.BY_WEEK,
      weekdays = listOf(0, 1, 0, 1, 0, 1, 0),
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(RecurrenceRule.Weekly(weekdays = listOf(0, 1, 0, 1, 0, 1, 0)), result.recurrence)
  }

  @Test
  fun `normalizes an unset repeatInterval of 0 to 1 for a weekly reminder`() {
    val reminder = Reminder(
      type = Reminder.BY_WEEK,
      weekdays = listOf(1, 0, 0, 0, 0, 0, 0),
      repeatInterval = 0L,
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(
      RecurrenceRule.Weekly(weekdays = listOf(1, 0, 0, 0, 0, 0, 0), repeatInterval = 1L),
      result.recurrence
    )
  }

  @Test
  fun `carries through an explicit every-N-weeks interval`() {
    val reminder = Reminder(
      type = Reminder.BY_WEEK,
      weekdays = listOf(1, 0, 0, 0, 0, 0, 0),
      repeatInterval = 3L,
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(
      RecurrenceRule.Weekly(weekdays = listOf(1, 0, 0, 0, 0, 0, 0), repeatInterval = 3L),
      result.recurrence
    )
  }

  @Test
  fun `maps a monthly reminder`() {
    val reminder = Reminder(
      type = Reminder.BY_MONTH,
      dayOfMonth = 5,
      repeatInterval = 1L,
      repeatLimit = -1,
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(
      RecurrenceRule.Monthly(dayOfMonth = 5, repeatInterval = 1L, repeatLimit = -1),
      result.recurrence
    )
  }

  @Test
  fun `maps a yearly reminder`() {
    val reminder = Reminder(
      type = Reminder.BY_DAY_OF_YEAR,
      dayOfMonth = 15,
      monthOfYear = 3,
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(RecurrenceRule.Yearly(dayOfMonth = 15, monthOfYear = 3), result.recurrence)
  }

  @Test
  fun `maps a location enter and location exit reminder`() {
    val enter = Reminder(type = Reminder.BY_LOCATION, startTime = GMT_TIME).toReminderV2()
    val exit = Reminder(type = Reminder.BY_OUT, startTime = GMT_TIME).toReminderV2()

    assertEquals(RecurrenceRule.LocationEnter, enter.recurrence)
    assertEquals(RecurrenceRule.LocationExit, exit.recurrence)
  }

  @Test
  fun `maps an icalendar reminder keeping the raw rrule string`() {
    val reminder = Reminder(
      type = Reminder.BY_RECUR,
      recurDataObject = "FREQ=DAILY;COUNT=5",
      startTime = GMT_TIME
    )

    val result = reminder.toReminderV2()

    assertEquals(RecurrenceRule.ICalendar(rrule = "FREQ=DAILY;COUNT=5"), result.recurrence)
  }

  @Test
  fun `parses the GMT-formatted date time string into a UTC LocalDateTime`() {
    val reminder = Reminder(type = Reminder.BY_DATE, startTime = GMT_TIME, eventTime = GMT_TIME)

    val result = reminder.toReminderV2()

    assertEquals(LocalDateTime.of(2026, 7, 22, 9, 0, 0), result.schedule.startDateTime)
    assertEquals(LocalDateTime.of(2026, 7, 22, 9, 0, 0), result.schedule.eventDateTime)
  }

  @Test
  fun `treats an empty groupUuId as no group`() {
    val reminder = Reminder(type = Reminder.BY_DATE, groupUuId = "", startTime = GMT_TIME)

    val result = reminder.toReminderV2()

    assertEquals(null, result.groupId)
  }

  companion object {
    private const val GMT_TIME = "2026-07-22 09:00:00.000+0000"
  }
}
