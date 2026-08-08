package com.github.naz013.logic.reminder.occurrence

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

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
