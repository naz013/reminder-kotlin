package com.elementary.tasks.reminder.scheduling

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Strategy for interval-based repeating reminders.
 * Uses repeatInterval property to calculate next occurrence.
 *
 * This strategy adds the repeatInterval (in milliseconds) to the current time
 * to determine when the reminder should fire next.
 */
class IntervalRepeatStrategy(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    val eventDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return null

    return recurrenceCalculator.findNextIntervalDateTime(
      eventDateTime = eventDateTime,
      intervalMillis = reminder.repeatInterval,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
    )
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return reminder.repeatInterval > 0 && !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    // Interval repeat reminders can be snoozed
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    // Interval repeat reminders can start immediately
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
