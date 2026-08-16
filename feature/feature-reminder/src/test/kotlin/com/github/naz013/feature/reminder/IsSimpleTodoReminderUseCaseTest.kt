package com.github.naz013.feature.reminder

import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.CalendarExportSettings
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.TaskExportSettings
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class IsSimpleTodoReminderUseCaseTest {
  private val useCase = IsSimpleTodoReminderUseCase()

  private fun simpleTodo(): ReminderV2 =
    ReminderV2(
      action = ReminderAction.Shopping,
      recurrence = RecurrenceRule.Once,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    )

  @Test
  fun `a simple todo is eligible`() {
    assertTrue(useCase(simpleTodo()))
  }

  @Test
  fun `non-Shopping action is not eligible`() {
    assertEquals(false, useCase(simpleTodo().copy(action = ReminderAction.None)))
  }

  @Test
  fun `recurrence other than Once is not eligible`() {
    assertEquals(false, useCase(simpleTodo().copy(recurrence = RecurrenceRule.LocationEnter)))
  }

  @Test
  fun `an event date-time is not eligible`() {
    val schedule = ReminderSchedule(startDateTime = LocalDateTime.now(), eventDateTime = LocalDateTime.now())
    assertEquals(false, useCase(simpleTodo().copy(schedule = schedule)))
  }

  @Test
  fun `a description is not eligible`() {
    assertEquals(false, useCase(simpleTodo().copy(description = "notes")))
  }

  @Test
  fun `a linked note is not eligible`() {
    assertEquals(false, useCase(simpleTodo().copy(noteId = "note-1")))
  }

  @Test
  fun `a calendar export is not eligible`() {
    val export = CalendarExportSettings(calendarId = 1, duration = 0, allDay = false)
    assertEquals(false, useCase(simpleTodo().copy(calendarExport = export)))
  }

  @Test
  fun `a task export is not eligible`() {
    assertEquals(false, useCase(simpleTodo().copy(taskExport = TaskExportSettings(taskListId = "list-1"))))
  }

  @Test
  fun `a location is not eligible`() {
    assertEquals(false, useCase(simpleTodo().copy(location = LocationSettings())))
  }

  @Test
  fun `attachments are not eligible`() {
    assertEquals(false, useCase(simpleTodo().copy(attachmentFiles = listOf("file.jpg"))))
  }

  @Test
  fun `places are not eligible`() {
    val place = Place(id = "place-1", name = "Home", syncState = SyncState.WaitingForUpload)
    assertEquals(false, useCase(simpleTodo().copy(places = listOf(place))))
  }

  @Test
  fun `non-default notification settings still ride along as eligible`() {
    val notification = NotificationSettingsOverride(priority = ReminderPriority.HIGH)
    assertTrue(useCase(simpleTodo().copy(notification = notification)))
  }
}
