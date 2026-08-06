package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.occurrence.RecurRepeatOccurrenceCalculator].
 * Covers RRULE-based repeating reminders ([RecurrenceRule.ICalendar]).
 */
class RecurRepeatOccurrenceCalculatorV2(
  private val recurEventManager: RecurEventManager,
) : ReminderOccurrenceCalculatorV2 {
  override suspend fun calculateOccurrences(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int,
  ): List<LocalDateTime> {
    if (numberOfOccurrences <= 0) {
      Logger.w(TAG, "calculateOccurrences: numberOfOccurrences must be positive")
      return emptyList()
    }

    val rruleString = (reminder.recurrence as? RecurrenceRule.ICalendar)?.rrule
    if (rruleString.isNullOrEmpty()) {
      Logger.w(TAG, "calculateOccurrences: rrule is empty")
      return emptyList()
    }

    val occurrences = mutableListOf<LocalDateTime>()
    var startDateTime = fromDateTime
    var endOfTheList = false

    repeat(numberOfOccurrences) {
      if (endOfTheList) return@repeat

      val nextOccurrence =
        recurEventManager.getNextAfterDateTime(
          startDateTime,
          rruleString,
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
    private const val TAG = "RecurRepeatOccurrCalcV2"
  }
}
