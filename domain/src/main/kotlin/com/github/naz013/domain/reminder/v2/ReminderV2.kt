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
  val notification: NotificationSettings = NotificationSettings(),
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
  val eventCount: Long = 0,
  val sync: SyncMetadata = SyncMetadata()
) {

  fun isLimited(): Boolean =
    (recurrence as? RecurrenceRule.Monthly)?.let { it.repeatLimit > 0 } ?: false

  fun isLimitExceed(): Boolean {
    val monthly = recurrence as? RecurrenceRule.Monthly ?: return false
    return isLimited() && (monthly.repeatLimit - eventCount - 1) < 0
  }
}

data class ReminderSchedule(
  val startDateTime: LocalDateTime,
  val eventDateTime: LocalDateTime? = null,
  val updatedAt: LocalDateTime? = null
)

data class NotificationSettings(
  val color: Int = 0,
  val vibrate: Boolean = false,
  val repeatNotification: Boolean = false,
  val volume: Int = -1,
  val useGlobalSettings: Boolean = true,
  val quietHoursFrom: String = "",
  val quietHoursTo: String = "",
  val activeHours: List<Int> = emptyList(),
  val delayMinutes: Int = 0,
  val priority: ReminderPriority = ReminderPriority.NORMAL,
  val remindBefore: Long = 0
)

enum class ReminderPriority {
  LOWEST,
  LOW,
  NORMAL,
  HIGH,
  HIGHEST
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
