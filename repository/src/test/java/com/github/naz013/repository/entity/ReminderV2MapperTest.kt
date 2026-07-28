package com.github.naz013.repository.entity

import com.github.naz013.domain.reminder.v2.CalendarExportSettings
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.reminder.v2.TaskExportSettings
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderV2MapperTest {

  @Test
  fun `toEntity then toDomain round trips a plain one-time reminder`() {
    val reminder = ReminderV2(
      uuId = "id-1",
      summary = "Buy milk",
      groupId = null,
      recurrence = RecurrenceRule.Once,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0)),
      action = ReminderAction.None
    )

    val roundTripped = reminder.toEntity().toDomain()

    assertEquals(reminder, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a monthly recurrence with a call action`() {
    val reminder = ReminderV2(
      uuId = "id-2",
      summary = "Pay rent",
      groupId = "group-1",
      recurrence = RecurrenceRule.Monthly(dayOfMonth = 5, repeatInterval = 1L, repeatLimit = -1),
      schedule = ReminderSchedule(
        startDateTime = LocalDateTime.of(2026, 8, 5, 10, 0),
        eventDateTime = LocalDateTime.of(2026, 8, 5, 10, 0)
      ),
      notification = NotificationSettingsOverride(priority = ReminderPriority.HIGH, vibrate = true),
      calendarExport = CalendarExportSettings(calendarId = 42L, duration = 3600L, allDay = false),
      taskExport = TaskExportSettings(taskListId = "list-1"),
      action = ReminderAction.Call(target = "+123456789"),
      sync = SyncMetadata(version = 3L, syncState = SyncState.Synced)
    )

    val roundTripped = reminder.toEntity().toDomain()

    assertEquals(reminder, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a daily recurrence with an until date`() {
    val reminder = ReminderV2(
      uuId = "id-4",
      summary = "Take vitamins",
      recurrence = RecurrenceRule.Daily(
        repeatInterval = 2L,
        until = LocalDateTime.of(2026, 12, 31, 0, 0)
      ),
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 8, 0)),
      action = ReminderAction.None
    )

    val roundTripped = reminder.toEntity().toDomain()

    assertEquals(reminder, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a relative monthly recurrence`() {
    val reminder = ReminderV2(
      uuId = "id-5",
      summary = "Team meeting",
      recurrence = RecurrenceRule.RelativeMonthly(weekday = 2, ordinal = 2),
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 14, 10, 0)),
      action = ReminderAction.None
    )

    val roundTripped = reminder.toEntity().toDomain()

    assertEquals(reminder, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips an alarm-style reminder with custom sound and vibration`() {
    val reminder = ReminderV2(
      uuId = "id-6",
      summary = "Take medication",
      recurrence = RecurrenceRule.Daily(),
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 8, 0)),
      notification = NotificationSettingsOverride(
        vibrate = true,
        vibrationPattern = listOf(0L, 200L, 100L, 200L),
        soundUri = "content://media/external/audio/media/42",
        category = ReminderNotificationCategory.ALARM,
        bypassDoNotDisturb = true,
        wakeScreen = true,
        lockScreenVisibility = LockScreenVisibility.PUBLIC
      ),
      action = ReminderAction.None
    )

    val roundTripped = reminder.toEntity().toDomain()

    assertEquals(reminder, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a location reminder without export settings`() {
    val reminder = ReminderV2(
      uuId = "id-3",
      summary = "Pick up parcel",
      recurrence = RecurrenceRule.LocationEnter,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 0, 0)),
      location = LocationSettings(isNotificationShown = true, isLocked = true),
      action = ReminderAction.None
    )

    val roundTripped = reminder.toEntity().toDomain()

    assertEquals(reminder, roundTripped)
    assertEquals(null, roundTripped.calendarExport)
    assertEquals(null, roundTripped.taskExport)
  }

  @Test
  fun `toEntity then toDomain round trips snoozeCount and lastShownAt`() {
    val reminder = ReminderV2(
      uuId = "id-7",
      summary = "Renew passport",
      recurrence = RecurrenceRule.Once,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0)),
      action = ReminderAction.None,
      snoozeCount = 3,
      lastShownAt = LocalDateTime.of(2026, 7, 22, 9, 5)
    )

    val roundTripped = reminder.toEntity().toDomain()

    assertEquals(reminder, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a null lastShownAt`() {
    val reminder = ReminderV2(
      uuId = "id-8",
      summary = "No notification shown yet",
      recurrence = RecurrenceRule.Once,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0)),
      action = ReminderAction.None
    )

    val roundTripped = reminder.toEntity().toDomain()

    assertEquals(reminder, roundTripped)
    assertEquals(0L, roundTripped.snoozeCount)
    assertEquals(null, roundTripped.lastShownAt)
  }
}
