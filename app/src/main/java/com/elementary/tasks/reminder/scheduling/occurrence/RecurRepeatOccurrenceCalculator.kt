package com.elementary.tasks.reminder.scheduling.occurrence

import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * Calculator for RRULE-based repeating reminders.
 *
 * Calculates occurrences based on iCalendar RRULE (Recurrence Rule) format.
 * This supports complex patterns like "every second Tuesday of the month".
 *
 * Note: This implementation provides a basic RRULE parser. For production use,
 * consider using a dedicated library like iCal4j or RFC5545-Recurrence.
 */
class RecurRepeatOccurrenceCalculator(
  private val recurEventManager: RecurEventManager,
) : ReminderOccurrenceCalculator {

  /**
   * Calculates occurrences for an RRULE-based repeating reminder.
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

    val rruleString = reminder.recurDataObject
    if (rruleString.isNullOrEmpty()) {
      Logger.w(TAG, "calculateOccurrences: recurDataObject is empty")
      return emptyList()
    }

    val occurrences = mutableListOf<LocalDateTime>()
    var startDateTime = fromDateTime
    var endOfTheList = false

    repeat(numberOfOccurrences) {
      if (endOfTheList) return@repeat

      val nextOccurrence = recurEventManager.getNextAfterDateTime(
        startDateTime,
        rruleString
      )

      if (nextOccurrence != null) {
        occurrences.add(nextOccurrence)
        startDateTime = nextOccurrence.plusSeconds(1) // Move past the last found occurrence
      } else {
        endOfTheList = true
      }
    }

    Logger.d(TAG, "calculateOccurrences: generated ${occurrences.size} occurrences")
    return occurrences
  }

  companion object {
    private const val TAG = "RecurRepeatOccurrCalc"
  }
}
