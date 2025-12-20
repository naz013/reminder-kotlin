package com.elementary.tasks.reminder.scheduling.occurrence

import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * Calculator for location-based reminders.
 *
 * Location-based reminders are triggered by geofence events (entering or leaving
 * a location), not by time. Therefore, they don't have predictable time-based
 * occurrences.
 */
class LocationBasedOccurrenceCalculator : ReminderOccurrenceCalculator {

  /**
   * Calculates occurrences for a location-based reminder.
   *
   * @param reminder The reminder to calculate occurrences for
   * @param fromDateTime The starting date/time for calculation
   * @param numberOfOccurrences The number of occurrences to calculate
   * @return Empty list, as location-based reminders don't have time-based occurrences
   */
  override suspend fun calculateOccurrences(
    reminder: Reminder,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int
  ): List<LocalDateTime> {
    Logger.d(TAG, "calculateOccurrences: LocationBasedOccurrenceCalculator - location-based reminders have no time occurrences")

    // Location-based reminders are triggered by geofence events, not time
    return emptyList()
  }

  companion object {
    private const val TAG = "LocationBasedOccurrCalc"
  }
}
