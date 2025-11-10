package com.elementary.tasks.reminder.scheduling

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Strategy for timer-based repeating reminders.
 * Uses from, to, and hours properties to determine next occurrence.
 *
 * This strategy calculates the next occurrence within a time window (from/to)
 * and specific hours list (e.g., every hour from 9 AM to 5 PM).
 */
class TimerRepeatStrategy(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    if (reminder.repeatInterval <= 0L) return null
    val eventDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return null
    val fromTime = dateTimeManager.toLocalTime(reminder.from)
    val toTime = dateTimeManager.toLocalTime(reminder.to)
    return recurrenceCalculator.findNextTimerDateTime(
      eventDateTime = eventDateTime,
      interval = reminder.repeatInterval,
      excludedHours = reminder.hours,
      excludedFromTime = fromTime,
      excludedToTime = toTime,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime()
    )
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return reminder.repeatInterval > 0L && !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
