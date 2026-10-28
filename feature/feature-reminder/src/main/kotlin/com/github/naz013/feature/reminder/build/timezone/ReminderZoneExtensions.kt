package com.github.naz013.feature.reminder.build.timezone

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/** The stored UTC instant as wall-clock time in the reminder's own zone (device zone when floating). */
internal fun DateTimeManager.utcToReminderLocal(
  reminder: ReminderV2,
  dateTime: LocalDateTime,
): LocalDateTime {
  val zone = DateTimeManager.parseFixedZone(reminder.timeZoneId)
  return if (zone != null) utcToLocal(dateTime, zone) else utcToLocal(dateTime)
}
