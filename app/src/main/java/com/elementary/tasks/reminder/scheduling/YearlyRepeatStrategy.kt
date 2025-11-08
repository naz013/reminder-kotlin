package com.elementary.tasks.reminder.scheduling

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
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
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null

    return modelDateTimeFormatter.getNextYearDayTime(reminder, fromDateTime)
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return reminder.dayOfMonth > 0 &&
           reminder.monthOfYear > 0 &&
           !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
