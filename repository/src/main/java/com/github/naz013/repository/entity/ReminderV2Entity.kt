package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.repository.converters.ListIntTypeConverter
import com.github.naz013.repository.converters.ListStringTypeConverter
import com.github.naz013.repository.converters.PlacesTypeConverter
import com.github.naz013.repository.converters.ReminderV2BuilderSchemeConverter
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
  ListIntTypeConverter::class,
  PlacesTypeConverter::class,
  ReminderV2ShopItemsConverter::class,
  ReminderV2BuilderSchemeConverter::class,
  ReminderV2VibrationPatternConverter::class
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
  val notification: NotificationSettingsColumns,
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

  val version: Long = 0L,
  val syncState: String
)

data class ReminderScheduleColumns(
  val startDateTime: Long,
  val eventDateTime: Long? = null,
  val updatedAt: Long? = null
)

data class NotificationSettingsColumns(
  val color: Int = 0,
  val vibrate: Boolean = false,
  val vibrationPattern: List<Long>? = null,
  val repeatNotification: Boolean = false,
  val volume: Int = -1,
  val soundUri: String? = null,
  val useGlobalSettings: Boolean = true,
  val quietHoursFrom: String = "",
  val quietHoursTo: String = "",
  val activeHours: List<Int> = emptyList(),
  val delayMinutes: Int = 0,
  val priority: String = "NORMAL",
  val category: String = "DEFAULT",
  val bypassDoNotDisturb: Boolean = false,
  val wakeScreen: Boolean = false,
  val lockScreenVisibility: String = "PRIVATE",
  val remindBefore: Long = 0
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
