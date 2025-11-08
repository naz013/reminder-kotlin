package com.elementary.tasks.reminder.scheduling

import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Strategy for location-based reminders.
 * Uses places property to trigger on geofence enter/exit.
 *
 * This strategy handles GPS-based reminders that trigger when entering
 * or leaving specified locations. They don't have traditional time-based
 * next occurrences.
 */
data object LocationBasedStrategy : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    return null
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return false
  }

  override fun requiresBackgroundService(reminder: Reminder): Boolean {
    return reminder.places.isNotEmpty()
  }

  override fun requiresTimeScheduling(reminder: Reminder): Boolean {
    return false
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    return false
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    return true
  }
}
