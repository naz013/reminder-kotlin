package com.elementary.tasks.reminder.scheduling

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
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
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    val eventDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return null

    return recurrenceCalculator.findNextMonthDayDateTime(
      eventDateTime = eventDateTime,
      dayOfMonth = reminder.dayOfMonth,
      interval = reminder.repeatInterval,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime()
    )
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return reminder.dayOfMonth >= 0 && !reminder.isLimitExceed()
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
