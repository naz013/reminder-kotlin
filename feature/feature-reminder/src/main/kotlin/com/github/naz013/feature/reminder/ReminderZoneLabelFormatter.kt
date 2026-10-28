package com.github.naz013.feature.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.reminder.build.timezone.supportsTimeZone
import org.threeten.bp.ZoneId

/** Builds the "09:00 Berlin" label for a fixed-zone reminder whose zone differs from the device zone. */
internal class ReminderZoneLabelFormatter(
  private val dateTimeManager: DateTimeManager,
  private val deviceZoneProvider: () -> ZoneId = { ZoneId.systemDefault() },
) {
  fun format(reminder: ReminderV2): String? {
    val zone = DateTimeManager.parseFixedZone(reminder.timeZoneId) ?: return null
    if (!reminder.recurrence.supportsTimeZone() || zone == deviceZoneProvider()) return null
    val eventDateTime = reminder.schedule.eventDateTime ?: return null
    val time = dateTimeManager.getTime(dateTimeManager.utcToLocal(eventDateTime, zone).toLocalTime())
    return "$time ${zone.id.substringAfterLast('/').replace('_', ' ')}"
  }
}
