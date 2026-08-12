package com.github.naz013.domain.reminder.migration

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.BuilderSchemeItem
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

/**
 * V2 -> V1 field mapping - the inverse of [toReminderV2]. Lets the reminder builder (Phase B)
 * construct/edit a `ReminderV2`-shaped in-progress reminder while still persisting through the
 * existing V1 write path (`ActivateReminderUseCase`/`SaveReminderUseCase`/scheduling/alarms/cloud
 * upload all still operate on V1 `Reminder` until Phase C retargets them). Not lossless in both
 * directions: `RecurrenceRule.RelativeMonthly` (a V2-only recurrence shape - "every Nth weekday of
 * the month") has no V1 representation at all and falls back to a plain by-day-of-month repeat
 * with no day set (see [RecurrenceRule.toFields]) - a documented, currently-unreachable gap, since
 * nothing produces `RelativeMonthly` until Phase B3 adds a builder path for it.
 */
fun ReminderV2.toReminder(): Reminder {
  val recurrenceFields = recurrence.toFields()
  val notif = notification
  return Reminder(
    summary = summary,
    noteId = noteId,
    groupUuId = groupId.orEmpty(),
    uuId = uuId,
    eventTime = schedule.eventDateTime?.let { formatUtcToGmt(it) } ?: "",
    startTime = formatUtcToGmt(schedule.startDateTime),
    eventCount = eventCount,
    color = notif.color ?: 0,
    delay = notif.delayMinutes ?: 0,
    vibrate = notif.vibrate ?: false,
    repeatNotification = notif.repeatNotification ?: false,
    exportToTasks = taskExport != null,
    exportToCalendar = calendarExport != null,
    from = notif.quietHoursFrom ?: "",
    to = notif.quietHoursTo ?: "",
    hours = notif.activeHours ?: emptyList(),
    volume = notif.volume ?: -1,
    dayOfMonth = recurrenceFields.dayOfMonth,
    monthOfYear = recurrenceFields.monthOfYear,
    repeatInterval = recurrenceFields.repeatInterval,
    repeatLimit = recurrenceFields.repeatLimit,
    after = recurrenceFields.after,
    weekdays = recurrenceFields.weekdays,
    type = recurrenceFields.base + action.toKind(),
    target = action.toTarget(),
    subject = action.toSubject(),
    attachmentFiles = attachmentFiles,
    places = places,
    shoppings = shoppingItems.map {
      ShopItem(
        summary = it.summary,
        isDeleted = it.isDeleted,
        isChecked = it.isChecked,
        uuId = it.uuId,
        createTime = formatUtcToGmt(it.createdAt)
      )
    },
    uniqueId = uniqueId,
    isActive = isActive,
    isRemoved = isRemoved,
    isNotificationShown = location?.isNotificationShown ?: false,
    isLocked = location?.isLocked ?: false,
    hasReminder = location?.hasDelayedReminder ?: false,
    duration = calendarExport?.duration ?: 0L,
    calendarId = calendarExport?.calendarId ?: 0L,
    remindBefore = notif.remindBefore ?: 0L,
    priority = (notif.priority ?: ReminderPriority.NORMAL).ordinal,
    updatedAt = schedule.updatedAt?.let { formatUtcToGmt(it) },
    taskListId = taskExport?.taskListId,
    recurDataObject = recurrenceFields.recurDataObject,
    allDay = calendarExport?.allDay ?: false,
    description = description,
    builderScheme = builderScheme?.map {
      BuilderSchemeItem(type = BiType.entries.getOrElse(it.type) { BiType.DATE }, position = it.position)
    },
    version = sync.version,
    syncState = sync.syncState
  )
}

private data class RecurrenceFields(
  val base: Int,
  val repeatInterval: Long = 0L,
  val repeatLimit: Int = -1,
  val weekdays: List<Int> = emptyList(),
  val dayOfMonth: Int = -1,
  val monthOfYear: Int = -1,
  val after: Long = 0L,
  val recurDataObject: String? = null
)

private fun RecurrenceRule.toFields(): RecurrenceFields = when (this) {
  RecurrenceRule.Once -> RecurrenceFields(base = Reminder.BY_DATE)
  is RecurrenceRule.Countdown ->
    RecurrenceFields(base = Reminder.BY_TIME, after = after, repeatInterval = repeatInterval, repeatLimit = repeatLimit)
  is RecurrenceRule.Daily ->
    RecurrenceFields(base = Reminder.BY_DATE, repeatInterval = repeatInterval, repeatLimit = repeatLimit)
  is RecurrenceRule.Weekly ->
    RecurrenceFields(
      base = Reminder.BY_WEEK,
      weekdays = weekdays,
      repeatInterval = repeatInterval,
      repeatLimit = repeatLimit
    )
  is RecurrenceRule.Monthly ->
    RecurrenceFields(
      base = Reminder.BY_MONTH,
      dayOfMonth = dayOfMonth,
      repeatInterval = repeatInterval,
      repeatLimit = repeatLimit
    )
  is RecurrenceRule.RelativeMonthly ->
    // No V1 base can express "nth weekday of month" - fall back to a plain monthly repeat with
    // no day-of-month set, rather than silently picking an arbitrary day. See file header.
    RecurrenceFields(
      base = Reminder.BY_MONTH,
      dayOfMonth = -1,
      repeatInterval = repeatInterval,
      repeatLimit = repeatLimit
    )
  is RecurrenceRule.Yearly ->
    RecurrenceFields(
      base = Reminder.BY_DAY_OF_YEAR,
      dayOfMonth = dayOfMonth,
      monthOfYear = monthOfYear,
      repeatInterval = repeatInterval,
      repeatLimit = repeatLimit
    )
  RecurrenceRule.LocationEnter -> RecurrenceFields(base = Reminder.BY_LOCATION)
  RecurrenceRule.LocationExit -> RecurrenceFields(base = Reminder.BY_OUT)
  is RecurrenceRule.ICalendar -> RecurrenceFields(base = Reminder.BY_RECUR, recurDataObject = rrule)
}

private fun ReminderAction.toKind(): Int = when (this) {
  ReminderAction.None -> Reminder.Action.NONE
  is ReminderAction.Call -> Reminder.Action.CALL
  is ReminderAction.Sms -> Reminder.Action.SMS
  is ReminderAction.App -> Reminder.Action.APP
  is ReminderAction.Link -> Reminder.Action.LINK
  is ReminderAction.Email -> Reminder.Action.EMAIL
  ReminderAction.Shopping -> Reminder.Action.SHOP
}

private fun ReminderAction.toTarget(): String = when (this) {
  is ReminderAction.Call -> target
  is ReminderAction.Sms -> target
  is ReminderAction.App -> target
  is ReminderAction.Link -> target
  is ReminderAction.Email -> target
  ReminderAction.None, ReminderAction.Shopping -> ""
}

private fun ReminderAction.toSubject(): String = when (this) {
  is ReminderAction.Sms -> subject
  is ReminderAction.Email -> subject
  else -> ""
}

private val REVERSE_GMT_FORMATTER: DateTimeFormatter =
  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)

private fun formatUtcToGmt(dateTime: LocalDateTime): String =
  ZonedDateTime.of(dateTime, ZoneOffset.UTC).format(REVERSE_GMT_FORMATTER)
