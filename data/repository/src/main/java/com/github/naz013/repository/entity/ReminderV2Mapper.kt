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
import com.github.naz013.logging.Logger
import com.google.gson.Gson
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

private const val TAG = "ReminderV2Mapper"

private val gson = Gson()

internal fun ReminderV2.toEntity(): ReminderV2Entity {
  val (recurrenceType, recurrencePayload) = recurrence.toColumns()
  val (actionType, actionTarget, actionSubject) = action.toColumns()
  return ReminderV2Entity(
    uuId = uuId,
    summary = summary,
    description = description,
    noteId = noteId,
    groupId = groupId,
    recurrenceType = recurrenceType,
    recurrencePayload = recurrencePayload,
    schedule = schedule.toColumns(),
    notification = notification.toColumns(),
    calendarExport = calendarExport?.toColumns(),
    taskExport = taskExport?.toColumns(),
    location = location?.toColumns(),
    actionType = actionType,
    actionTarget = actionTarget,
    actionSubject = actionSubject,
    attachmentFiles = attachmentFiles,
    places = places.map { PlaceEntity(it.copy(syncState = SyncState.Synced)) },
    shoppingItems = shoppingItems,
    builderScheme = builderScheme,
    uniqueId = uniqueId,
    isActive = isActive,
    isRemoved = isRemoved,
    isPinned = isPinned,
    eventCount = eventCount,
    snoozeCount = snoozeCount,
    lastShownAt = lastShownAt?.toEpochMillisUtc(),
    version = sync.version,
    syncState = sync.syncState.name,
    offlineOnly = offlineOnly
  )
}

internal fun ReminderV2Entity.toDomain(): ReminderV2 {
  return ReminderV2(
    uuId = uuId,
    summary = summary,
    description = description,
    noteId = noteId,
    groupId = groupId,
    recurrence = toRecurrenceRule(recurrenceType, recurrencePayload),
    schedule = schedule.toDomain(),
    notification = notification.toDomain(),
    calendarExport = calendarExport?.toDomain(),
    taskExport = taskExport?.toDomain(),
    location = location?.toDomain(),
    action = toReminderAction(actionType, actionTarget, actionSubject),
    attachmentFiles = attachmentFiles,
    places = places.map { it.toDomain() },
    shoppingItems = shoppingItems,
    builderScheme = builderScheme,
    uniqueId = uniqueId,
    isActive = isActive,
    isRemoved = isRemoved,
    isPinned = isPinned,
    eventCount = eventCount,
    snoozeCount = snoozeCount,
    lastShownAt = lastShownAt?.toLocalDateTimeUtc(),
    sync = SyncMetadata(version = version, syncState = SyncState.valueOf(syncState)),
    offlineOnly = offlineOnly
  )
}

private fun ReminderSchedule.toColumns(): ReminderScheduleColumns = ReminderScheduleColumns(
  startDateTime = startDateTime.toEpochMillisUtc(),
  eventDateTime = eventDateTime?.toEpochMillisUtc(),
  updatedAt = updatedAt?.toEpochMillisUtc()
)

private fun ReminderScheduleColumns.toDomain(): ReminderSchedule = ReminderSchedule(
  startDateTime = startDateTime.toLocalDateTimeUtc(),
  eventDateTime = eventDateTime?.toLocalDateTimeUtc(),
  updatedAt = updatedAt?.toLocalDateTimeUtc()
)

internal fun NotificationSettingsOverride.toColumns(): NotificationSettingsOverrideColumns =
  NotificationSettingsOverrideColumns(
    color = color,
    vibrate = vibrate,
    vibrationPattern = vibrationPattern,
    repeatNotification = repeatNotification,
    volume = volume,
    soundUri = soundUri,
    quietHoursFrom = quietHoursFrom,
    quietHoursTo = quietHoursTo,
    activeHours = activeHours,
    delayMinutes = delayMinutes,
    priority = priority?.name,
    category = category?.name,
    bypassDoNotDisturb = bypassDoNotDisturb,
    wakeScreen = wakeScreen,
    lockScreenVisibility = lockScreenVisibility?.name,
    remindBefore = remindBefore
  )

internal fun NotificationSettingsOverrideColumns.toDomain(): NotificationSettingsOverride =
  NotificationSettingsOverride(
    color = color,
    vibrate = vibrate,
    vibrationPattern = vibrationPattern,
    repeatNotification = repeatNotification,
    volume = volume,
    soundUri = soundUri,
    quietHoursFrom = quietHoursFrom,
    quietHoursTo = quietHoursTo,
    activeHours = activeHours,
    delayMinutes = delayMinutes,
    priority = priority?.let { runCatching { ReminderPriority.valueOf(it) }.getOrNull() },
    category = category?.let { runCatching { ReminderNotificationCategory.valueOf(it) }.getOrNull() },
    bypassDoNotDisturb = bypassDoNotDisturb,
    wakeScreen = wakeScreen,
    lockScreenVisibility = lockScreenVisibility?.let {
      runCatching { LockScreenVisibility.valueOf(it) }.getOrNull()
    },
    remindBefore = remindBefore
  )

private fun CalendarExportSettings.toColumns(): CalendarExportSettingsColumns =
  CalendarExportSettingsColumns(
    calendarId = calendarId,
    duration = duration,
    allDay = allDay
  )

private fun CalendarExportSettingsColumns.toDomain(): CalendarExportSettings =
  CalendarExportSettings(
    calendarId = calendarId ?: 0L,
    duration = duration ?: 0L,
    allDay = allDay ?: false
  )

private fun TaskExportSettings.toColumns(): TaskExportSettingsColumns = TaskExportSettingsColumns(
  taskListId = taskListId
)

private fun TaskExportSettingsColumns.toDomain(): TaskExportSettings = TaskExportSettings(
  taskListId = taskListId ?: ""
)

private fun LocationSettings.toColumns(): LocationSettingsColumns = LocationSettingsColumns(
  isNotificationShown = isNotificationShown,
  isLocked = isLocked,
  hasDelayedReminder = hasDelayedReminder
)

private fun LocationSettingsColumns.toDomain(): LocationSettings = LocationSettings(
  isNotificationShown = isNotificationShown ?: false,
  isLocked = isLocked ?: false,
  hasDelayedReminder = hasDelayedReminder ?: false
)

private fun RecurrenceRule.toColumns(): Pair<String, String> = when (this) {
  is RecurrenceRule.Once -> "ONCE" to ""
  is RecurrenceRule.Countdown -> "COUNTDOWN" to gson.toJson(this)
  is RecurrenceRule.Daily -> "DAILY" to gson.toJson(this)
  is RecurrenceRule.Weekly -> "WEEKLY" to gson.toJson(this)
  is RecurrenceRule.Monthly -> "MONTHLY" to gson.toJson(this)
  is RecurrenceRule.RelativeMonthly -> "RELATIVE_MONTHLY" to gson.toJson(this)
  is RecurrenceRule.Yearly -> "YEARLY" to gson.toJson(this)
  is RecurrenceRule.LocationEnter -> "LOCATION_ENTER" to ""
  is RecurrenceRule.LocationExit -> "LOCATION_EXIT" to ""
  is RecurrenceRule.ICalendar -> "ICALENDAR" to gson.toJson(this)
}

/** Falls back to [RecurrenceRule.Once] (and logs) instead of throwing on a payload it can't parse,
 * e.g. a row written before a fix to `app/proguard-rules.pro` kept Gson-reflected field names -
 * one unreadable row must not take down the whole reminder list. */
private fun toRecurrenceRule(type: String, payload: String): RecurrenceRule = runCatching {
  when (type) {
    "ONCE" -> RecurrenceRule.Once
    "COUNTDOWN" -> gson.fromJson(payload, RecurrenceRule.Countdown::class.java)
    "DAILY" -> gson.fromJson(payload, RecurrenceRule.Daily::class.java)
    "WEEKLY" -> gson.fromJson(payload, RecurrenceRule.Weekly::class.java).also {
      requireNotNull(it.weekdays) { "weekdays is null" }
    }
    "MONTHLY" -> gson.fromJson(payload, RecurrenceRule.Monthly::class.java)
    "RELATIVE_MONTHLY" -> gson.fromJson(payload, RecurrenceRule.RelativeMonthly::class.java)
    "YEARLY" -> gson.fromJson(payload, RecurrenceRule.Yearly::class.java)
    "LOCATION_ENTER" -> RecurrenceRule.LocationEnter
    "LOCATION_EXIT" -> RecurrenceRule.LocationExit
    "ICALENDAR" -> gson.fromJson(payload, RecurrenceRule.ICalendar::class.java).also {
      requireNotNull(it.rrule) { "rrule is null" }
    }
    else -> RecurrenceRule.Once
  }
}.getOrElse { e ->
  Logger.e(TAG, "Failed to parse recurrence rule, type=$type, payload=$payload", e)
  RecurrenceRule.Once
}

private fun ReminderAction.toColumns(): Triple<String, String, String> = when (this) {
  is ReminderAction.None -> Triple("NONE", "", "")
  is ReminderAction.Call -> Triple("CALL", target, "")
  is ReminderAction.Sms -> Triple("SMS", target, subject)
  is ReminderAction.Link -> Triple("LINK", target, "")
  is ReminderAction.App -> Triple("APP", target, "")
  is ReminderAction.Email -> Triple("EMAIL", target, subject)
  is ReminderAction.Shopping -> Triple("SHOPPING", "", "")
}

private fun toReminderAction(type: String, target: String, subject: String): ReminderAction =
  when (type) {
    "NONE" -> ReminderAction.None
    "CALL" -> ReminderAction.Call(target)
    "SMS" -> ReminderAction.Sms(target, subject)
    "LINK" -> ReminderAction.Link(target)
    "APP" -> ReminderAction.App(target)
    "EMAIL" -> ReminderAction.Email(target, subject)
    "SHOPPING" -> ReminderAction.Shopping
    else -> ReminderAction.None
  }

internal fun LocalDateTime.toEpochMillisUtc(): Long = toInstant(ZoneOffset.UTC).toEpochMilli()

internal fun Long.toLocalDateTimeUtc(): LocalDateTime =
  LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
