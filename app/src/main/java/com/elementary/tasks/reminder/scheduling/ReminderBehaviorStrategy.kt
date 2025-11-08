package com.elementary.tasks.reminder.scheduling

import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Defines the behavior strategy for reminder occurrence calculation and management.
 *
 * This interface abstracts the logic for determining when a reminder should fire next,
 * allowing different repeat patterns to be handled without relying on the reminderType property.
 */
interface ReminderBehaviorStrategy {

  /**
   * Calculates the next occurrence time for the reminder.
   *
   * @param reminder The reminder to calculate next occurrence for
   * @param fromDateTime The starting date/time for calculation
   * @return The next occurrence LocalDateTime, or null if no more occurrences
   */
  fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? = null

  /**
   * Determines if the reminder can be skipped to a future occurrence.
   *
   * @param reminder The reminder to check
   * @return true if the reminder has future occurrences and can be skipped
   */
  fun canSkip(reminder: Reminder): Boolean = false

  /**
   * Determines if this reminder requires special service handling (e.g., GPS tracking).
   *
   * @param reminder The reminder to check
   * @return true if special service is required
   */
  fun requiresBackgroundService(reminder: Reminder): Boolean = false

  /**
   * Determines if the reminder needs time-based scheduling.
   *
   * @param reminder The reminder to check
   * @return true if time-based scheduling is needed
   */
  fun requiresTimeScheduling(reminder: Reminder): Boolean = true

  /**
   * Determines if the reminder can be snoozed.
   *
   * @param reminder The reminder to check
   * @return true if snoozing is allowed
   */
  fun canSnooze(reminder: Reminder): Boolean = true

  /**
   * Determines if the reminder can start immediately.
   *
   * @param reminder The reminder to check
   * @return true if it can start immediately
   */
  fun canStartImmediately(reminder: Reminder): Boolean = true
}
