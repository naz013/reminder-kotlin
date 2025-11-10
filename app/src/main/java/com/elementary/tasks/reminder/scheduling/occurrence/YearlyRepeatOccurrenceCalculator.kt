package com.elementary.tasks.reminder.scheduling.occurrence

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * Calculator for yearly repeating reminders.
 *
 * Calculates occurrences based on a specific day and month each year
 * (e.g., March 15th every year). Handles leap years appropriately.
 */
class YearlyRepeatOccurrenceCalculator(
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) : ReminderOccurrenceCalculator {

  /**
   * Calculates occurrences for a yearly repeating reminder.
   *
   * @param reminder The reminder to calculate occurrences for
   * @param fromDateTime The starting date/time for calculation
   * @param numberOfOccurrences The number of occurrences to calculate
   * @return List of calculated occurrence times
   */
  override suspend fun calculateOccurrences(
    reminder: Reminder,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int
  ): List<LocalDateTime> {
    // Validate input
    if (numberOfOccurrences <= 0) {
      Logger.w(TAG, "calculateOccurrences: numberOfOccurrences must be positive")
      return emptyList()
    }

    if (reminder.dayOfMonth < 0 || reminder.monthOfYear < 0) {
      Logger.w(TAG, "calculateOccurrences: dayOfMonth and monthOfYear must be positive")
      return emptyList()
    }

    // Calculate the remaining limit
    val remainingLimit = if (reminder.isLimited()) {
      maxOf(reminder.repeatLimit - reminder.eventCount.toInt(), 0)
    } else {
      Int.MAX_VALUE
    }

    val maxOccurrences = minOf(numberOfOccurrences, remainingLimit)

    if (maxOccurrences <= 0) {
      Logger.d(TAG, "calculateOccurrences: limit already exceeded")
      return emptyList()
    }

    val occurrences = mutableListOf<LocalDateTime>()
    var startDateTime = fromDateTime

    repeat(maxOccurrences) {
      val nextOccurrence = recurrenceCalculator.getNextYearDayDateTime(
        eventDateTime = startDateTime,
        monthOfYear = reminder.dayOfMonth,
        dayOfMonth = reminder.monthOfYear,
        interval = reminder.repeatInterval,
      )

      occurrences.add(nextOccurrence)
      startDateTime = nextOccurrence
    }

    Logger.d(TAG, "calculateOccurrences: generated ${occurrences.size} occurrences")
    return occurrences
  }
  companion object {
    private const val TAG = "YearlyRepeatOccurrCalc"
  }
}
