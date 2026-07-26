package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.occurrence.LocationBasedOccurrenceCalculator].
 * Location-based reminders are triggered by geofence events, not time, so they have no
 * predictable time-based occurrences.
 */
class LocationBasedOccurrenceCalculatorV2 : ReminderOccurrenceCalculatorV2 {
  override suspend fun calculateOccurrences(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int,
  ): List<LocalDateTime> {
    Logger.d(TAG, "calculateOccurrences: LocationBasedOccurrenceCalculatorV2 - location-based reminders have no time occurrences")
    return emptyList()
  }

  companion object {
    private const val TAG = "LocationBasedOccurrCalcV2"
  }
}
