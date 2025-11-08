package com.elementary.tasks.reminder.scheduling

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
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
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null

    var time = modelDateTimeFormatter.generateNextTimer(reminder, false)

    // Skip past occurrences until we find a current/future one
    while (!dateTimeManager.isCurrent(time)) {
      time = modelDateTimeFormatter.generateNextTimer(reminder, false)
    }

    return time
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return reminder.from.isNotEmpty() &&
           reminder.to.isNotEmpty() &&
           reminder.hours.isNotEmpty() &&
           !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
