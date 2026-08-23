package com.github.naz013.domain.reminder.v2

import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import org.threeten.bp.LocalDateTime
import java.util.Random
import java.util.UUID

data class ReminderV2(
  val uuId: String = UUID.randomUUID().toString(),
  val summary: String = "",
  val description: String? = null,
  val noteId: String = "",
  val groupId: String? = null,
  val recurrence: RecurrenceRule = RecurrenceRule.Once,
  val schedule: ReminderSchedule,
  val notification: NotificationSettingsOverride = NotificationSettingsOverride(),
  val calendarExport: CalendarExportSettings? = null,
  val taskExport: TaskExportSettings? = null,
  val location: LocationSettings? = null,
  val action: ReminderAction = ReminderAction.None,
  val attachmentFiles: List<String> = emptyList(),
  val places: List<Place> = emptyList(),
  val shoppingItems: List<ShopItemV2> = emptyList(),
  val builderScheme: List<BuilderSchemeItemV2>? = null,
  val uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
  val isActive: Boolean = true,
  val isRemoved: Boolean = false,
  val isPinned: Boolean = false,
  val eventCount: Long = 0,
  /** Number of times this reminder has been snoozed; powers `WorkflowTrigger.ReminderSnoozedNTimes`. */
  val snoozeCount: Long = 0,
  /** When the notification for this reminder was last actually shown to the user; cleared on
   * complete/snooze. Powers `WorkflowTrigger.ReminderUnacknowledgedFor`. */
  val lastShownAt: LocalDateTime? = null,
  val sync: SyncMetadata = SyncMetadata(),
  /** True only if set at creation time (BuildReminder/TodoEdit) - excludes this reminder from
   * cloud sync and Local Backup until the user explicitly opts back in via "Sync to cloud". */
  val offlineOnly: Boolean = false
) {

  fun isLimited(): Boolean = recurrence.repeatLimitOrDefault() > 0

  fun isLimitExceed(): Boolean = isLimited() && (recurrence.repeatLimitOrDefault() - eventCount - 1) < 0
}

data class ReminderSchedule(
  val startDateTime: LocalDateTime,
  val eventDateTime: LocalDateTime? = null,
  val updatedAt: LocalDateTime? = null
)

/** The fully-resolved shape: what global Settings holds, and what [NotificationSettingsOverride.resolve] returns. */
data class NotificationSettings(
  val color: Int = 0,
  val vibrate: Boolean = false,
  val vibrationPattern: List<Long>? = null,
  val repeatNotification: Boolean = false,
  val volume: Int = -1,
  val soundUri: String? = null,
  val quietHoursFrom: String = "",
  val quietHoursTo: String = "",
  val activeHours: List<Int> = emptyList(),
  val delayMinutes: Int = 0,
  val priority: ReminderPriority = ReminderPriority.NORMAL,
  val category: ReminderNotificationCategory = ReminderNotificationCategory.DEFAULT,
  val bypassDoNotDisturb: Boolean = false,
  val wakeScreen: Boolean = false,
  val lockScreenVisibility: LockScreenVisibility = LockScreenVisibility.PRIVATE,
  val remindBefore: Long = 0
)

enum class ReminderPriority {
  LOWEST,
  LOW,
  NORMAL,
  HIGH,
  HIGHEST
}

/** Maps to [android.app.Notification.CATEGORY_REMINDER]/[android.app.Notification.CATEGORY_ALARM]/etc. */
enum class ReminderNotificationCategory {
  DEFAULT,
  ALARM,
  EVENT,
  CALL
}

/** Maps to [android.app.Notification.VISIBILITY_PUBLIC]/[android.app.Notification.VISIBILITY_PRIVATE]/[android.app.Notification.VISIBILITY_SECRET]. */
enum class LockScreenVisibility {
  PUBLIC,
  PRIVATE,
  SECRET
}

data class CalendarExportSettings(
  val calendarId: Long,
  val duration: Long,
  val allDay: Boolean
)

data class TaskExportSettings(
  val taskListId: String
)

data class LocationSettings(
  val isNotificationShown: Boolean = false,
  val isLocked: Boolean = false,
  val hasDelayedReminder: Boolean = false
)

data class SyncMetadata(
  val version: Long = 0L,
  val syncState: SyncState = SyncState.WaitingForUpload
)
