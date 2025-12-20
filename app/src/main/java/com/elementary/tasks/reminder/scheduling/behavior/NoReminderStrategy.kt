package com.elementary.tasks.reminder.scheduling.behavior

import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Strategy for simple reminders with no date/time dependencies.
 */
data object NoReminderStrategy : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    // No next occurrence for simple reminders
    return null
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return false
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    // Simple date reminders can be snoozed
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    // Simple date reminders can start immediately
    return true
  }
}
