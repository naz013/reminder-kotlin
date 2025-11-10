package com.elementary.tasks.reminder.scheduling.occurrence

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * Calculator for weekday-based repeating reminders.
 *
 * Calculates occurrences based on selected weekdays (e.g., Monday, Wednesday, Friday).
 * The reminder time remains the same, but dates advance to the next selected weekday.
 */
class WeekdayRepeatOccurrenceCalculator(
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) : ReminderOccurrenceCalculator {

  /**
   * Calculates occurrences for a weekday-based repeating reminder.
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

    if (reminder.weekdays.isEmpty()) {
      Logger.w(TAG, "calculateOccurrences: weekdays list is empty")
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
      val nextOccurrence = recurrenceCalculator.getNextDayOfWeekDateTime(
        eventDateTime = startDateTime,
        weekdays = reminder.weekdays,
      )

      occurrences.add(nextOccurrence)
      startDateTime = nextOccurrence
    }

    Logger.d(TAG, "calculateOccurrences: generated ${occurrences.size} occurrences")
    return occurrences
  }

  companion object {
    private const val TAG = "WeekdayRepeatOccurrCalc"
  }
}
