package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.occurrence.ReminderOccurrenceCalculator].
 */
interface ReminderOccurrenceCalculatorV2 {
  suspend fun calculateOccurrences(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int,
  ): List<LocalDateTime>
}
