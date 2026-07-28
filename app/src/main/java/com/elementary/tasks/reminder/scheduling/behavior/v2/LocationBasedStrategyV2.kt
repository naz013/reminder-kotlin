package com.elementary.tasks.reminder.scheduling.behavior.v2

import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.behavior.LocationBasedStrategy].
 * Strategy for location-based reminders (GPS enter/exit) - no traditional time-based next occurrence.
 */
data object LocationBasedStrategyV2 : ReminderBehaviorStrategyV2 {
  override fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? = null

  override fun canSkip(reminder: ReminderV2): Boolean = false

  override fun requiresBackgroundService(reminder: ReminderV2): Boolean = reminder.places.isNotEmpty()

  override fun requiresTimeScheduling(reminder: ReminderV2): Boolean = false

  override fun canSnooze(reminder: ReminderV2): Boolean = false

  override fun canStartImmediately(reminder: ReminderV2): Boolean = true
}
