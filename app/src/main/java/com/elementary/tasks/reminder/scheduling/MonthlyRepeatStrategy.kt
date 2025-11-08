package com.elementary.tasks.reminder.scheduling

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import org.threeten.bp.LocalDateTime

/**
 * Strategy for monthly repeating reminders.
 * Uses dayOfMonth property to determine next occurrence.
 *
 * This strategy advances to the next month while maintaining the same day of month
 * (e.g., the 15th of every month).
 */
class MonthlyRepeatStrategy(
  private val dateTimeManager: DateTimeManager,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null

    return modelDateTimeFormatter.getNewNextMonthDayTime(reminder, fromDateTime)
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return reminder.dayOfMonth > 0 && !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    // Monthly repeat reminders can be snoozed
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    // Monthly repeat reminders can start immediately
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
