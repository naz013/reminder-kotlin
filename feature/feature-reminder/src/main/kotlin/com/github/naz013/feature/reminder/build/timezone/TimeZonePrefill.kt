package com.github.naz013.feature.reminder.build.timezone

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.bi.BiGroup
import com.github.naz013.logic.reminder.ReminderPreferences
import org.threeten.bp.ZoneId

/** Decides when the "New reminders use: fixed to the current zone" setting adds a zone to a new reminder. */
internal class TimeZonePrefill(
  private val reminderPreferences: ReminderPreferences,
  private val deviceZoneProvider: () -> ZoneId = { ZoneId.systemDefault() },
) {
  /** The device zone id to pre-fill, or null when the setting is off or no zone-capable item is in [used] yet. */
  fun zoneIdToPrefill(used: List<BuilderItem<*>>): String? {
    if (!reminderPreferences.useFixedZoneForNewReminders) return null
    val types = used.map { it.biType }.toSet()
    if (BiType.TIME_ZONE in types) return null
    val supportsZone = types.any { it in ZONE_TYPES } || used.any { it.biGroup == BiGroup.ICAL }
    if (!supportsZone) return null
    return DateTimeManager.parseFixedZone(deviceZoneProvider().id)?.id
  }

  private companion object {
    val ZONE_TYPES = setOf(BiType.DATE, BiType.DAYS_OF_WEEK, BiType.DAY_OF_MONTH, BiType.DAY_OF_YEAR)
  }
}
