package com.github.naz013.domain.reminder.migration

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.v2.BuilderSchemeItemV2
import com.github.naz013.domain.reminder.v2.CalendarExportSettings
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.reminder.v2.TaskExportSettings
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

/**
 * V1 -> V2 field mapping. Originally written as a one-time backfill mapper; also used by
 * `ReminderRepositoryImpl` to mirror every V1 write into `ReminderV2Repository`, keeping
 * `ReminderV2` rows fresh while `ReminderV2` isn't yet the write-of-record anywhere in the app,
 * and by the reminder builder to convert an in-memory `Reminder` (e.g. one deserialized from an
 * import/share Intent with no repository row yet) for display. Not part of ReminderV2's
 * steady-state entity<->domain mapping (that lives in the `repository` module's entity mappers).
 */
fun Reminder.toReminderV2(): ReminderV2 {
  val startDateTime = parseGmtToUtc(startTime) ?: LocalDateTime.now(ZoneOffset.UTC)
  return ReminderV2(
    uuId = uuId,
    summary = summary,
    description = description,
    noteId = noteId,
    groupId = groupUuId.takeIf { it.isNotEmpty() },
    recurrence = toRecurrenceRule(),
    schedule = ReminderSchedule(
      startDateTime = startDateTime,
      eventDateTime = parseGmtToUtc(eventTime),
      updatedAt = parseGmtToUtc(updatedAt)
    ),
    notification = NotificationSettingsOverride(
      color = color,
      vibrate = vibrate,
      repeatNotification = repeatNotification,
      volume = volume,
      quietHoursFrom = from,
      quietHoursTo = to,
      activeHours = hours,
      delayMinutes = delay,
      priority = toReminderPriority(priority),
      remindBefore = remindBefore
    ),
    calendarExport = if (exportToCalendar) {
      CalendarExportSettings(calendarId = calendarId, duration = duration, allDay = allDay)
    } else {
      null
    },
    taskExport = if (exportToTasks && !taskListId.isNullOrEmpty()) {
      TaskExportSettings(taskListId = taskListId.orEmpty())
    } else {
      null
    },
    location = if (Reminder.isGpsType(type)) {
      LocationSettings(
        isNotificationShown = isNotificationShown,
        isLocked = isLocked,
        hasDelayedReminder = hasReminder
      )
    } else {
      null
    },
    action = toReminderAction(),
    attachmentFiles = attachmentFiles,
    places = places,
    shoppingItems = shoppings.map {
      ShopItemV2(
        uuId = it.uuId,
        summary = it.summary,
        isChecked = it.isChecked,
        isDeleted = it.isDeleted,
        createdAt = parseGmtToUtc(it.createTime) ?: LocalDateTime.now(ZoneOffset.UTC)
      )
    },
    builderScheme = builderScheme?.map { BuilderSchemeItemV2(type = it.type.ordinal, position = it.position) },
    uniqueId = uniqueId,
    isActive = isActive,
    isRemoved = isRemoved,
    eventCount = eventCount,
    sync = SyncMetadata(version = version, syncState = syncState)
  )
}

private fun Reminder.toRecurrenceRule(): RecurrenceRule = when {
  Reminder.isBase(type, Reminder.BY_DATE) ->
    if (repeatInterval > 0) {
      RecurrenceRule.Daily(repeatInterval = repeatInterval, repeatLimit = repeatLimit)
    } else {
      RecurrenceRule.Once
    }
  Reminder.isBase(type, Reminder.BY_TIME) ->
    RecurrenceRule.Countdown(after = after, repeatInterval = repeatInterval, repeatLimit = repeatLimit)
  Reminder.isBase(type, Reminder.BY_WEEK) ->
    RecurrenceRule.Weekly(
      weekdays = weekdays,
      repeatInterval = normalizeInterval(repeatInterval),
      repeatLimit = repeatLimit
    )
  Reminder.isBase(type, Reminder.BY_MONTH) ->
    RecurrenceRule.Monthly(
      dayOfMonth = dayOfMonth,
      repeatInterval = normalizeInterval(repeatInterval),
      repeatLimit = repeatLimit
    )
  Reminder.isBase(type, Reminder.BY_DAY_OF_YEAR) ->
    RecurrenceRule.Yearly(
      dayOfMonth = dayOfMonth,
      monthOfYear = monthOfYear,
      repeatInterval = normalizeInterval(repeatInterval),
      repeatLimit = repeatLimit
    )
  Reminder.isBase(type, Reminder.BY_LOCATION) -> RecurrenceRule.LocationEnter
  Reminder.isBase(type, Reminder.BY_OUT) -> RecurrenceRule.LocationExit
  Reminder.isBase(type, Reminder.BY_RECUR) -> RecurrenceRule.ICalendar(recurDataObject.orEmpty())
  else -> RecurrenceRule.Once
}

/** V1's shared `repeatInterval` field defaults to 0 when unused; 0 has no meaning as a recurrence
 * interval, so treat it the same as "every occurrence" (interval 1) instead of carrying the 0 forward. */
private fun normalizeInterval(repeatInterval: Long): Long = if (repeatInterval > 0) repeatInterval else 1L

private fun Reminder.toReminderAction(): ReminderAction = when (type % Reminder.BY_DATE) {
  Reminder.Action.CALL -> ReminderAction.Call(target)
  Reminder.Action.SMS -> ReminderAction.Sms(target, subject)
  Reminder.Action.APP -> ReminderAction.App(target)
  Reminder.Action.LINK -> ReminderAction.Link(target)
  Reminder.Action.EMAIL -> ReminderAction.Email(target, subject)
  Reminder.Action.SHOP -> ReminderAction.Shopping
  else -> ReminderAction.None
}

private fun toReminderPriority(priority: Int): ReminderPriority =
  ReminderPriority.entries.getOrElse(priority) { ReminderPriority.NORMAL }

private val GMT_FORMATTER: DateTimeFormatter =
  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)

private fun parseGmtToUtc(value: String?): LocalDateTime? {
  if (value.isNullOrEmpty()) return null
  return runCatching {
    ZonedDateTime.parse(value, GMT_FORMATTER).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
  }.getOrNull()
}
