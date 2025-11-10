package com.elementary.tasks.reminder.scheduling.occurrence

import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * Calculator for simple one-time date/time reminders.
 *
 * Since simple reminders have no repeat pattern, this calculator returns
 * an empty list as there are no future occurrences after the initial event.
 */
class SimpleDateOccurrenceCalculator : ReminderOccurrenceCalculator {

  /**
   * Calculates occurrences for a simple one-time reminder.
   *
   * @param reminder The reminder to calculate occurrences for
   * @param fromDateTime The starting date/time for calculation
   * @param numberOfOccurrences The number of occurrences to calculate
   * @return Empty list, as simple reminders have no repeat pattern
   */
  override suspend fun calculateOccurrences(
    reminder: Reminder,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int
  ): List<LocalDateTime> {
    Logger.d(TAG, "calculateOccurrences: SimpleDateOccurrenceCalculator - no repeat pattern")

    // Simple reminders fire once and have no future occurrences
    return emptyList()
  }

  companion object {
    private const val TAG = "SimpleDateOccurrCalc"
  }
}
