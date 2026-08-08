package com.github.naz013.logic.reminder.behavior

import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

interface ReminderBehaviorStrategyV2 {
  fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? = null

  fun canSkip(reminder: ReminderV2): Boolean = false

  fun requiresBackgroundService(reminder: ReminderV2): Boolean = false

  fun requiresTimeScheduling(reminder: ReminderV2): Boolean = true

  fun canSnooze(reminder: ReminderV2): Boolean = true

  fun canStartImmediately(reminder: ReminderV2): Boolean = true
}
