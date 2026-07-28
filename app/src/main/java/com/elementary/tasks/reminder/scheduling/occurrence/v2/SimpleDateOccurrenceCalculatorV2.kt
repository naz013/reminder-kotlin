package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.occurrence.SimpleDateOccurrenceCalculator].
 * Simple one-time reminders have no repeat pattern, so there are no future occurrences.
 */
class SimpleDateOccurrenceCalculatorV2 : ReminderOccurrenceCalculatorV2 {
  override suspend fun calculateOccurrences(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int,
  ): List<LocalDateTime> {
    Logger.d(TAG, "calculateOccurrences: SimpleDateOccurrenceCalculatorV2 - no repeat pattern")
    return emptyList()
  }

  companion object {
    private const val TAG = "SimpleDateOccurrCalcV2"
  }
}
