package com.elementary.tasks.reminder.scheduling.occurrence

import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * Calculator for reminders with no time-based scheduling.
 *
 * These are simple reminders that don't have date/time dependencies
 * and therefore don't have future occurrences.
 */
class NoReminderOccurrenceCalculator : ReminderOccurrenceCalculator {

  /**
   * Calculates occurrences for a reminder with no scheduling.
   *
   * @param reminder The reminder to calculate occurrences for
   * @param fromDateTime The starting date/time for calculation
   * @param numberOfOccurrences The number of occurrences to calculate
   * @return Empty list, as these reminders have no scheduled occurrences
   */
  override suspend fun calculateOccurrences(
    reminder: Reminder,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int
  ): List<LocalDateTime> {
    Logger.d(TAG, "calculateOccurrences: NoReminderOccurrenceCalculator - no scheduled occurrences")

    // No scheduled occurrences for this type
    return emptyList()
  }

  companion object {
    private const val TAG = "NoReminderOccurrCalc"
  }
}
