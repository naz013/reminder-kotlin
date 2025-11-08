package com.elementary.tasks.reminder.scheduling

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
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
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) : ReminderBehaviorStrategy {

  override fun calculateNextOccurrence(
    reminder: Reminder,
    fromDateTime: LocalDateTime
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null

    return modelDateTimeFormatter.getNextWeekdayTime(reminder, fromDateTime)
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
