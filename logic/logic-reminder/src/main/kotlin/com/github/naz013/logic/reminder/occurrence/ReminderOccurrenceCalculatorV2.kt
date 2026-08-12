package com.github.naz013.logic.reminder.occurrence

import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * Calculates future occurrence times for a `ReminderV2`'s recurrence pattern.
 */
interface ReminderOccurrenceCalculatorV2 {
  suspend fun calculateOccurrences(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int,
  ): List<LocalDateTime>
}
