package com.github.naz013.domain.reminder.migration

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderV2ToReminderMapperTest {

  @Test
  fun `maps RecurrenceRule Once to a plain BY_DATE type`() {
    val reminder = reminderV2(recurrence = RecurrenceRule.Once).toReminder()

    assertEquals(Reminder.BY_DATE, reminder.type)
    assertEquals(0L, reminder.repeatInterval)
  }

  @Test
  fun `maps RecurrenceRule Countdown carrying a repeat interval back onto BY_TIME's repeatInterval`() {
    val reminder = reminderV2(
      recurrence = RecurrenceRule.Countdown(after = 60_000L, repeatInterval = 7_200_000L, repeatLimit = 3)
    ).toReminder()

    assertEquals(Reminder.BY_TIME, reminder.type)
    assertEquals(60_000L, reminder.after)
    assertEquals(7_200_000L, reminder.repeatInterval)
    assertEquals(3, reminder.repeatLimit)
  }

  @Test
  fun `maps RecurrenceRule Daily carrying a sub-day millis interval back onto repeatInterval`() {
    val reminder = reminderV2(
      recurrence = RecurrenceRule.Daily(repeatInterval = 21_600_000L, repeatLimit = 5)
    ).toReminder()

    assertEquals(Reminder.BY_DATE, reminder.type)
    assertEquals(21_600_000L, reminder.repeatInterval)
    assertEquals(5, reminder.repeatLimit)
  }

  @Test
  fun `maps RecurrenceRule Weekly`() {
    val reminder = reminderV2(
      recurrence = RecurrenceRule.Weekly(weekdays = listOf(0, 1, 0, 1, 0, 1, 0), repeatInterval = 2)
    ).toReminder()

    assertEquals(Reminder.BY_WEEK, reminder.type)
    assertEquals(listOf(0, 1, 0, 1, 0, 1, 0), reminder.weekdays)
    assertEquals(2L, reminder.repeatInterval)
  }

  @Test
  fun `maps RecurrenceRule Monthly`() {
    val reminder = reminderV2(recurrence = RecurrenceRule.Monthly(dayOfMonth = 12)).toReminder()

    assertEquals(Reminder.BY_MONTH, reminder.type)
    assertEquals(12, reminder.dayOfMonth)
  }

  @Test
  fun `falls back to a dayOfMonth-less monthly repeat for RecurrenceRule RelativeMonthly`() {
    val reminder = reminderV2(
      recurrence = RecurrenceRule.RelativeMonthly(weekday = 2, ordinal = 2, repeatInterval = 1)
    ).toReminder()

    assertEquals(Reminder.BY_MONTH, reminder.type)
    assertEquals(-1, reminder.dayOfMonth)
  }

  @Test
  fun `maps RecurrenceRule Yearly`() {
    val reminder = reminderV2(recurrence = RecurrenceRule.Yearly(dayOfMonth = 4, monthOfYear = 7)).toReminder()

    assertEquals(Reminder.BY_DAY_OF_YEAR, reminder.type)
    assertEquals(4, reminder.dayOfMonth)
    assertEquals(7, reminder.monthOfYear)
  }

  @Test
  fun `maps RecurrenceRule LocationEnter and LocationExit`() {
    val enter = reminderV2(recurrence = RecurrenceRule.LocationEnter).toReminder()
    val exit = reminderV2(recurrence = RecurrenceRule.LocationExit).toReminder()

    assertEquals(Reminder.BY_LOCATION, enter.type)
    assertEquals(Reminder.BY_OUT, exit.type)
  }

  @Test
  fun `maps RecurrenceRule ICalendar keeping the raw rrule string`() {
    val reminder = reminderV2(recurrence = RecurrenceRule.ICalendar(rrule = "FREQ=DAILY;COUNT=5")).toReminder()

    assertEquals(Reminder.BY_RECUR, reminder.type)
    assertEquals("FREQ=DAILY;COUNT=5", reminder.recurDataObject)
  }

  @Test
  fun `maps every ReminderAction case onto the V1 type kind and target fields`() {
    assertEquals(Reminder.Action.CALL, reminderV2(action = ReminderAction.Call("+123")).toReminder().type % Reminder.BY_DATE)
    assertEquals("+123", reminderV2(action = ReminderAction.Call("+123")).toReminder().target)

    val sms = reminderV2(action = ReminderAction.Sms("+123", "hi")).toReminder()
    assertEquals(Reminder.Action.SMS, sms.type % Reminder.BY_DATE)
    assertEquals("+123", sms.target)
    assertEquals("hi", sms.subject)

    val app = reminderV2(action = ReminderAction.App("com.example.app")).toReminder()
    assertEquals(Reminder.Action.APP, app.type % Reminder.BY_DATE)
    assertEquals("com.example.app", app.target)

    val link = reminderV2(action = ReminderAction.Link("https://example.com")).toReminder()
    assertEquals(Reminder.Action.LINK, link.type % Reminder.BY_DATE)
    assertEquals("https://example.com", link.target)

    val email = reminderV2(action = ReminderAction.Email("a@b.com", "subj")).toReminder()
    assertEquals(Reminder.Action.EMAIL, email.type % Reminder.BY_DATE)
    assertEquals("a@b.com", email.target)
    assertEquals("subj", email.subject)

    assertEquals(Reminder.Action.SHOP, reminderV2(action = ReminderAction.Shopping).toReminder().type % Reminder.BY_DATE)
    assertEquals(Reminder.Action.NONE, reminderV2(action = ReminderAction.None).toReminder().type % Reminder.BY_DATE)
  }

  @Test
  fun `formats the UTC schedule back into a GMT-formatted string`() {
    val reminder = reminderV2(
      schedule = ReminderSchedule(
        startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0, 0),
        eventDateTime = LocalDateTime.of(2026, 7, 22, 9, 0, 0)
      )
    ).toReminder()

    assertEquals("2026-07-22 09:00:00.000+0000", reminder.startTime)
    assertEquals("2026-07-22 09:00:00.000+0000", reminder.eventTime)
  }

  @Test
  fun `resolves null notification overrides to the same V1 defaults the forward mapper reads`() {
    val reminder = reminderV2(notification = NotificationSettingsOverride()).toReminder()

    assertEquals(0, reminder.color)
    assertEquals(-1, reminder.volume)
    assertEquals(0L, reminder.remindBefore)
    assertEquals(2, reminder.priority) // ReminderPriority.NORMAL.ordinal
  }

  @Test
  fun `round-trips a call reminder through V1 to V2 and back`() {
    val original = Reminder(
      type = Reminder.BY_WEEK + Reminder.Action.CALL,
      weekdays = listOf(1, 0, 0, 0, 0, 0, 0),
      target = "+123456789",
      summary = "Call mom",
      startTime = GMT_TIME
    )

    val roundTripped = original.toReminderV2().toReminder()

    assertEquals(original.type, roundTripped.type)
    assertEquals(original.weekdays, roundTripped.weekdays)
    assertEquals(original.target, roundTripped.target)
    assertEquals(original.summary, roundTripped.summary)
  }

  private fun reminderV2(
    recurrence: RecurrenceRule = RecurrenceRule.Once,
    action: ReminderAction = ReminderAction.None,
    schedule: ReminderSchedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0, 0)),
    notification: NotificationSettingsOverride = NotificationSettingsOverride(),
  ) = ReminderV2(
    recurrence = recurrence,
    action = action,
    schedule = schedule,
    notification = notification,
  )

  companion object {
    private const val GMT_TIME = "2026-07-22 09:00:00.000+0000"
  }
}
