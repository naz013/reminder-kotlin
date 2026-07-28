package com.elementary.tasks.reminder.scheduling.behavior.v2

import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.behavior.ReminderBehaviorStrategy].
 * Not yet wired to any production call site (Phase C sub-phase C1) - see the migration plan.
 */
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
