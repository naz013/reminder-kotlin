package com.elementary.tasks.reminder.scheduling.behavior

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
    fromDateTime: LocalDateTime,
  ): LocalDateTime? = null

  override fun canSkip(reminder: Reminder): Boolean = false

  override fun requiresBackgroundService(reminder: Reminder): Boolean = reminder.places.isNotEmpty()

  override fun requiresTimeScheduling(reminder: Reminder): Boolean = false

  override fun canSnooze(reminder: Reminder): Boolean = false

  override fun canStartImmediately(reminder: Reminder): Boolean = true
}
