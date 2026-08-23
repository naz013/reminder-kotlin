package com.github.naz013.files.model

import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.BuilderSchemeItemV2
import com.google.gson.annotations.SerializedName

data class ReminderV2Json(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("uuId")
  val uuId: String,
  @SerializedName("summary")
  val summary: String = "",
  @SerializedName("description")
  val description: String? = null,
  @SerializedName("noteId")
  val noteId: String = "",
  @SerializedName("groupId")
  val groupId: String? = null,
  @SerializedName("recurrenceType")
  val recurrenceType: String,
  @SerializedName("recurrencePayload")
  val recurrencePayload: String,
  @SerializedName("startDateTime")
  val startDateTime: String,
  @SerializedName("eventDateTime")
  val eventDateTime: String? = null,
  @SerializedName("updatedAt")
  val updatedAt: String? = null,
  @SerializedName("notification")
  val notification: NotificationSettingsOverrideJson,
  @SerializedName("calendarExport")
  val calendarExport: CalendarExportSettingsJson? = null,
  @SerializedName("taskExport")
  val taskExport: TaskExportSettingsJson? = null,
  @SerializedName("location")
  val location: LocationSettingsJson? = null,
  @SerializedName("actionType")
  val actionType: String,
  @SerializedName("actionTarget")
  val actionTarget: String = "",
  @SerializedName("actionSubject")
  val actionSubject: String = "",
  @SerializedName("attachmentFiles")
  val attachmentFiles: List<String> = emptyList(),
  @SerializedName("places")
  val places: List<Place> = emptyList(),
  @SerializedName("shoppingItems")
  val shoppingItems: List<ShopItemV2Json> = emptyList(),
  @SerializedName("builderScheme")
  val builderScheme: List<BuilderSchemeItemV2>? = null,
  @SerializedName("uniqueId")
  val uniqueId: Int = 0,
  @SerializedName("isActive")
  val isActive: Boolean = true,
  @SerializedName("isRemoved")
  val isRemoved: Boolean = false,
  @SerializedName("isPinned")
  val isPinned: Boolean = false,
  @SerializedName("eventCount")
  val eventCount: Long = 0,
  @SerializedName("versionId")
  val version: Long = 0L,
  @SerializedName("offlineOnly")
  val offlineOnly: Boolean = false
)

/** Wire shape for [com.github.naz013.domain.reminder.v2.NotificationSettingsOverride] вЂ” every field optional. */
data class NotificationSettingsOverrideJson(
  @SerializedName("color")
  val color: Int? = null,
  @SerializedName("vibrate")
  val vibrate: Boolean? = null,
  @SerializedName("vibrationPattern")
  val vibrationPattern: List<Long>? = null,
  @SerializedName("repeatNotification")
  val repeatNotification: Boolean? = null,
  @SerializedName("volume")
  val volume: Int? = null,
  @SerializedName("soundUri")
  val soundUri: String? = null,
  @SerializedName("quietHoursFrom")
  val quietHoursFrom: String? = null,
  @SerializedName("quietHoursTo")
  val quietHoursTo: String? = null,
  @SerializedName("activeHours")
  val activeHours: List<Int>? = null,
  @SerializedName("delayMinutes")
  val delayMinutes: Int? = null,
  @SerializedName("priority")
  val priority: String? = null,
  @SerializedName("category")
  val category: String? = null,
  @SerializedName("bypassDoNotDisturb")
  val bypassDoNotDisturb: Boolean? = null,
  @SerializedName("wakeScreen")
  val wakeScreen: Boolean? = null,
  @SerializedName("lockScreenVisibility")
  val lockScreenVisibility: String? = null,
  @SerializedName("remindBefore")
  val remindBefore: Long? = null
)

data class CalendarExportSettingsJson(
  @SerializedName("calendarId")
  val calendarId: Long,
  @SerializedName("duration")
  val duration: Long,
  @SerializedName("allDay")
  val allDay: Boolean
)

data class TaskExportSettingsJson(
  @SerializedName("taskListId")
  val taskListId: String
)

data class LocationSettingsJson(
  @SerializedName("isNotificationShown")
  val isNotificationShown: Boolean = false,
  @SerializedName("isLocked")
  val isLocked: Boolean = false,
  @SerializedName("hasDelayedReminder")
  val hasDelayedReminder: Boolean = false
)

data class ShopItemV2Json(
  @SerializedName("uuId")
  val uuId: String,
  @SerializedName("summary")
  val summary: String = "",
  @SerializedName("isChecked")
  val isChecked: Boolean = false,
  @SerializedName("isDeleted")
  val isDeleted: Boolean = false,
  @SerializedName("createdAt")
  val createdAt: String
)
