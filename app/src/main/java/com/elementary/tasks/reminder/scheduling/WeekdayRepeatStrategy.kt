package com.elementary.tasks.reminder.scheduling

import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

/**
 * Strategy for weekday-based repeating reminders.
 * Uses weekdays list to determine next occurrence.
 *
 * This strategy finds the next occurrence based on the selected weekdays
 * (e.g., Monday, Wednesday, Friday) from the reminder's weekdays list.
 */
class WeekdayRepeatStrategy(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    if (reminder.weekdays.isEmpty()) return null
    val eventDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return null

    return recurrenceCalculator.findNextDayOfWeekDateTime(
      eventDateTime = eventDateTime,
      weekdays = reminder.weekdays,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
    )
  }

  override fun canSkip(reminder: Reminder): Boolean {
    return reminder.weekdays.isNotEmpty() && !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: Reminder): Boolean {
    return true
  }

  override fun canStartImmediately(reminder: Reminder): Boolean {
    return dateTimeManager.isCurrent(reminder.eventTime)
  }
}
