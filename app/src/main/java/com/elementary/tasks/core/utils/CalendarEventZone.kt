package com.elementary.tasks.core.utils

import com.github.naz013.datecalc.DateTimeManager

internal object CalendarEventZone {
  private const val UTC = "UTC"

  /** Calendar Provider needs an IANA id for EVENT_TIMEZONE; all-day events must be in UTC. */
  fun resolveId(
    reminderZoneId: String?,
    allDay: Boolean,
    deviceZoneId: String,
  ): String =
    when {
      allDay -> UTC
      else -> DateTimeManager.parseFixedZone(reminderZoneId)?.id ?: deviceZoneId
    }
}
