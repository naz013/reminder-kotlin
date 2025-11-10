package com.elementary.tasks.reminder.scheduling.behavior

import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Strategy for RRULE-based repeating reminders.
 * Uses recurDataObject property (RRULE string) to determine next occurrence.
 *
 * This strategy parses the RRULE (Recurrence Rule) string to calculate
 * complex recurrence patterns (e.g., "every second Tuesday of the month").
 */
class RecurRepeatStrategy(
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    return recurEventManager.getNextAfterDateTime(
      fromDateTime,
      reminder.recurDataObject
    )
  }

  override fun canSkip(reminder: Reminder): Boolean {
    if (reminder.recurDataObject.isNullOrEmpty()) return false

    val currentEventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
    return recurEventManager.getNextAfterDateTime(
      currentEventTime,
      reminder.recurDataObject
    ) != null
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
