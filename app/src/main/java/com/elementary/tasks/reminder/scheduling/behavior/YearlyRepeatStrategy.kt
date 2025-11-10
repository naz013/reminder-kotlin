package com.elementary.tasks.reminder.scheduling.behavior

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Strategy for yearly repeating reminders.
 * Uses dayOfMonth and monthOfYear properties to determine next occurrence.
 *
 * This strategy advances to the next year while maintaining the same day and month
 * (e.g., March 15th every year).
 */
class YearlyRepeatStrategy(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    val eventDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return null

    return recurrenceCalculator.findNextYearDayDateTime(
      eventDateTime = eventDateTime,
      dayOfMonth = reminder.dayOfMonth,
      monthOfYear = reminder.monthOfYear,
      interval = reminder.repeatInterval,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
    )
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
