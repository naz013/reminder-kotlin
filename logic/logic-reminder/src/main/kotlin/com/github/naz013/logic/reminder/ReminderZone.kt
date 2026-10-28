package com.github.naz013.logic.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

/**
 * Zone-aware conversions for a [ReminderV2]. A floating reminder (null or unknown zone id) goes through the
 * device-zone [DateTimeManager] overloads, so its behaviour is exactly the pre-time-zone one.
 */
internal fun ReminderV2.fixedZone(): ZoneId? = DateTimeManager.parseFixedZone(timeZoneId)

internal fun DateTimeManager.reminderUtcToLocal(
  reminder: ReminderV2,
  dateTime: LocalDateTime,
): LocalDateTime = reminder.fixedZone()?.let { utcToLocal(dateTime, it) } ?: utcToLocal(dateTime)

internal fun DateTimeManager.reminderLocalToUtc(
  reminder: ReminderV2,
  dateTime: LocalDateTime,
): LocalDateTime = reminder.fixedZone()?.let { localToUtc(dateTime, it) } ?: localToUtc(dateTime)

internal fun DateTimeManager.reminderToMillis(
  reminder: ReminderV2,
  dateTime: LocalDateTime,
): Long = reminder.fixedZone()?.let { toMillis(dateTime, it) } ?: toMillis(dateTime)

internal fun DateTimeManager.reminderNow(reminder: ReminderV2): LocalDateTime =
  reminder.fixedZone()?.let { getCurrentDateTime(it) } ?: getCurrentDateTime()

internal fun DateTimeManager.isEventInFuture(reminder: ReminderV2): Boolean {
  val zone = reminder.fixedZone()
  return reminder.schedule.eventDateTime?.let { utc ->
    if (zone == null) {
      isCurrent(utcToLocal(utc))
    } else {
      utcToLocal(utc, zone).isAfter(getCurrentDateTime(zone))
    }
  } ?: false
}
