package com.github.naz013.logic.reminder.today

import com.github.naz013.domain.CalendarEvent
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.reminder.v2.CalendarExportSettings
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class TodayDeduplicatorTest {

  private val at = LocalDateTime.of(2026, 10, 17, 15, 0)

  @Test
  fun `drops calendar event matched by exported device event id`() {
    val reminder = reminderItem("r1")
    val event = eventItem(deviceEventId = 10)
    val link = CalendarEvent(reminderId = "r1", event = "e", eventId = 10, allDay = false)

    val result = dedupToday(listOf(reminder), listOf(event), emptyList(), listOf(link))

    assertTrue(result.calendarEvents.isEmpty())
    assertTrue(result.reminders.single().alsoInCalendar)
  }

  @Test
  fun `drops calendar event by calendar start and title when ids differ`() {
    val reminder = reminderItem("r1", summary = "Dentist", calendarId = 3)
    val event = eventItem(deviceEventId = 11, calendarId = 3, title = "  dentist ")

    val result = dedupToday(listOf(reminder), listOf(event), emptyList(), emptyList())

    assertTrue(result.calendarEvents.isEmpty())
  }

  @Test
  fun `drops google task whose uuid equals a reminder uuid and flags the reminder`() {
    val reminder = reminderItem("r1")
    val task = TodayItem.Task(GoogleTask(taskId = "t", uuId = "r1"), at, TodayBucket.Anytime)

    val result = dedupToday(listOf(reminder), emptyList(), listOf(task), emptyList())

    assertTrue(result.tasks.isEmpty())
    assertTrue(result.reminders.single().alsoInTasks)
  }

  @Test
  fun `hides mirrored event and task of an active reminder that is not shown today`() {
    val hidden = reminderItem("later", at = at.plusDays(3))
    val event = eventItem(deviceEventId = 10)
    val task = TodayItem.Task(GoogleTask(taskId = "t", uuId = "later"), at, TodayBucket.Anytime)
    val link = CalendarEvent(reminderId = "later", event = "e", eventId = 10, allDay = false)

    val result = dedupToday(
      reminders = emptyList(),
      matchCandidates = listOf(hidden),
      calendarEvents = listOf(event),
      tasks = listOf(task),
      calendarLinks = listOf(link),
    )

    assertTrue(result.calendarEvents.isEmpty())
    assertTrue(result.tasks.isEmpty())
    assertTrue(result.reminders.isEmpty())
  }

  @Test
  fun `keeps unrelated event and task and leaves reminder flags off`() {
    val reminder = reminderItem("r1")
    val event = eventItem(deviceEventId = 10)
    val task = TodayItem.Task(GoogleTask(taskId = "t", uuId = "other"), at, TodayBucket.Anytime)

    val result = dedupToday(listOf(reminder), listOf(event), listOf(task), emptyList())

    assertEquals(1, result.calendarEvents.size)
    assertEquals(1, result.tasks.size)
    assertFalse(result.reminders.single().alsoInCalendar)
    assertFalse(result.reminders.single().alsoInTasks)
  }

  private fun reminderItem(
    id: String,
    summary: String = "Reminder $id",
    calendarId: Long? = null,
    at: LocalDateTime = this.at,
  ) = TodayItem.Reminder(
    reminder = ReminderV2(
      uuId = id,
      summary = summary,
      schedule = ReminderSchedule(startDateTime = at, eventDateTime = at),
      calendarExport = calendarId?.let { CalendarExportSettings(it, 0, false) },
    ),
    dateTime = at,
    isOverdue = false,
  )

  private fun eventItem(
    deviceEventId: Long,
    calendarId: Long = 1,
    title: String = "Event",
  ) = TodayItem.CalendarEvent(
    event = GoogleCalendarEvent(
      deviceEventId = deviceEventId,
      calendarId = calendarId,
      calendarName = "cal",
      title = title,
      description = "",
      startDateTime = at,
      endDateTime = null,
      allDay = false,
      rrule = "",
    ),
    dateTime = at,
    endDateTime = null,
  )
}
