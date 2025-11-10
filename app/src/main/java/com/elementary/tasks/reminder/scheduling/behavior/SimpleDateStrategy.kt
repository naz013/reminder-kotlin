package com.elementary.tasks.reminder.scheduling.behavior

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Strategy for simple one-time date/time reminders.
 * No repeat pattern - fires once at the specified time.
 */
class SimpleDateStrategy(
  private val dateTimeManager: DateTimeManager
) : ReminderBehaviorStrategy {

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
    // Simple date reminders can start immediately if the event time is current
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
