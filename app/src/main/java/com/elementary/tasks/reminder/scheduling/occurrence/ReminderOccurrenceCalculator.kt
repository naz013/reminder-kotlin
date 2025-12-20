package com.elementary.tasks.reminder.scheduling.occurrence

import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

interface ReminderOccurrenceCalculator {
  suspend fun calculateOccurrences(
    reminder: Reminder,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int
  ): List<LocalDateTime>
}
