package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.repository.converters.ListStringTypeConverter
import com.github.naz013.repository.converters.PlacesTypeConverter
import com.github.naz013.repository.converters.ReminderV2BuilderSchemeConverter
import com.github.naz013.repository.converters.ReminderV2NullableIntListConverter
import com.github.naz013.repository.converters.ReminderV2ShopItemsConverter
import com.github.naz013.repository.converters.ReminderV2VibrationPatternConverter
import com.github.naz013.domain.reminder.v2.BuilderSchemeItemV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import java.util.UUID

@Entity(
  tableName = "ReminderV2",
  indices = [
    Index(value = ["isActive", "isRemoved", "sched_eventDateTime"]),
    Index(value = ["recurrenceType"]),
    Index(value = ["groupId"]),
    Index(value = ["noteId"]),
    Index(value = ["syncState"])
  ]
)
@TypeConverters(
  ListStringTypeConverter::class,
  PlacesTypeConverter::class,
  ReminderV2ShopItemsConverter::class,
  ReminderV2BuilderSchemeConverter::class,
  ReminderV2VibrationPatternConverter::class,
  ReminderV2NullableIntListConverter::class
)
@Keep
internal data class ReminderV2Entity(
  @PrimaryKey
  val uuId: String = UUID.randomUUID().toString(),
  val summary: String = "",
  val description: String? = null,
  val noteId: String = "",
  val groupId: String? = null,

  val recurrenceType: String,
  val recurrencePayload: String,

  @Embedded(prefix = "sched_")
  val schedule: ReminderScheduleColumns,
  @Embedded(prefix = "notif_")
  val notification: NotificationSettingsOverrideColumns,
  @Embedded(prefix = "cal_")
  val calendarExport: CalendarExportSettingsColumns?,
  @Embedded(prefix = "task_")
  val taskExport: TaskExportSettingsColumns?,
  @Embedded(prefix = "loc_")
  val location: LocationSettingsColumns?,

  val actionType: String,
  val actionTarget: String = "",
  val actionSubject: String = "",

  val attachmentFiles: List<String> = emptyList(),
  val places: List<PlaceEntity> = emptyList(),
  val shoppingItems: List<ShopItemV2> = emptyList(),
  val builderScheme: List<BuilderSchemeItemV2>? = null,

  val uniqueId: Int = 0,
  val isActive: Boolean = true,
  val isRemoved: Boolean = false,
  val eventCount: Long = 0,
  val snoozeCount: Long = 0,
  val lastShownAt: Long? = null,

  val version: Long = 0L,
  val syncState: String
)

data class ReminderScheduleColumns(
  val startDateTime: Long,
  val eventDateTime: Long? = null,
  val updatedAt: Long? = null
)

/** Mirrors [com.github.naz013.domain.reminder.v2.NotificationSettingsOverride] — all columns nullable. */
data class NotificationSettingsOverrideColumns(
  val color: Int? = null,
  val vibrate: Boolean? = null,
  val vibrationPattern: List<Long>? = null,
  val repeatNotification: Boolean? = null,
  val volume: Int? = null,
  val soundUri: String? = null,
  val quietHoursFrom: String? = null,
  val quietHoursTo: String? = null,
  val activeHours: List<Int>? = null,
  val delayMinutes: Int? = null,
  val priority: String? = null,
  val category: String? = null,
  val bypassDoNotDisturb: Boolean? = null,
  val wakeScreen: Boolean? = null,
  val lockScreenVisibility: String? = null,
  val remindBefore: Long? = null
)

data class CalendarExportSettingsColumns(
  val calendarId: Long? = null,
  val duration: Long? = null,
  val allDay: Boolean? = null
)

data class TaskExportSettingsColumns(
  val taskListId: String? = null
)

data class LocationSettingsColumns(
  val isNotificationShown: Boolean? = null,
  val isLocked: Boolean? = null,
  val hasDelayedReminder: Boolean? = null
)
