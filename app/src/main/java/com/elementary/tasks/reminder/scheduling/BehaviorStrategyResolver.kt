package com.elementary.tasks.reminder.scheduling

import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter

/**
 * Resolves the appropriate behavior strategy based on Reminder properties.
 * Does NOT use reminderType property.
 *
 * This resolver analyzes the intrinsic properties of a reminder (such as places,
 * weekdays, repeatInterval, recurDataObject, etc.) to determine which strategy
 * should be used to handle its scheduling behavior.
 */
class BehaviorStrategyResolver(
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) {

  /**
   * Determines the behavior strategy based on intrinsic Reminder properties.
   *
   * Priority order:
   * 1. Location-based (if has places)
   * 2. Shopping list (if has shopping items without reminder)
   * 3. Recurrence (if has recurDataObject)
   * 4. Timer (if has from/to/hours)
   * 5. Yearly (if has dayOfMonth + monthOfYear)
   * 6. Monthly (if has dayOfMonth)
   * 7. Weekly (if has weekdays)
   * 8. Interval repeat (if has repeatInterval > 0)
   * 9. Simple date (default)
   *
   * @param reminder The reminder to analyze
   * @return The appropriate behavior strategy
   */
  fun resolve(reminder: Reminder): ReminderBehaviorStrategy {
    return when {
      // Location-based reminders have places list
      reminder.places.isNotEmpty() && reminder.eventTime.isNotEmpty() && dateTimeManager.isCurrent(
        reminder.eventTime
      ) -> {
        SimpleDateStrategy(dateTimeManager)
      }

      // Location-based reminders
      reminder.places.isNotEmpty() -> {
        LocationBasedStrategy
      }

      // Shopping list without time-based reminder
      reminder.shoppings.isNotEmpty() && !hasAnyRepeatProperties(reminder) -> {
        NoStrategy
      }

      // Recurrence pattern (RRULE)
      !reminder.recurDataObject.isNullOrEmpty() -> {
        RecurRepeatStrategy(dateTimeManager, recurEventManager)
      }

      // Timer-based (has time window and hours)
      hasTimerProperties(reminder) -> {
        TimerRepeatStrategy(dateTimeManager, modelDateTimeFormatter)
      }

      // Yearly repeat (specific day and month)
      hasYearlyProperties(reminder) -> {
        YearlyRepeatStrategy(dateTimeManager, modelDateTimeFormatter)
      }

      // Monthly repeat (specific day of month)
      hasMonthlyProperties(reminder) -> {
        MonthlyRepeatStrategy(dateTimeManager, modelDateTimeFormatter)
      }

      // Weekly repeat (specific weekdays)
      hasWeekdayProperties(reminder) -> {
        WeekdayRepeatStrategy(dateTimeManager, modelDateTimeFormatter)
      }

      // Interval-based repeat (every X milliseconds)
      reminder.repeatInterval > 0 -> {
        IntervalRepeatStrategy(dateTimeManager)
      }

      // Simple one-time date/time reminder
      else -> {
        SimpleDateStrategy(dateTimeManager)
      }
    }
  }

  /**
   * Checks if reminder has timer-specific properties.
   * Timer reminders have: from time, to time, and hours list.
   *
   * @param reminder The reminder to check
   * @return true if reminder has timer properties
   */
  private fun hasTimerProperties(reminder: Reminder): Boolean {
    return reminder.from.isNotEmpty() &&
      reminder.to.isNotEmpty() &&
      reminder.hours.isNotEmpty()
  }

  /**
   * Checks if reminder has yearly repeat properties.
   * Yearly reminders have: dayOfMonth and monthOfYear set.
   *
   * @param reminder The reminder to check
   * @return true if reminder has yearly properties
   */
  private fun hasYearlyProperties(reminder: Reminder): Boolean {
    return reminder.dayOfMonth > 0 && reminder.monthOfYear > 0
  }

  /**
   * Checks if reminder has monthly repeat properties.
   * Monthly reminders have: dayOfMonth set but not monthOfYear (or monthOfYear = 0).
   *
   * @param reminder The reminder to check
   * @return true if reminder has monthly properties
   */
  private fun hasMonthlyProperties(reminder: Reminder): Boolean {
    return reminder.dayOfMonth > 0 && reminder.monthOfYear == 0
  }

  /**
   * Checks if reminder has weekday repeat properties.
   * Weekly reminders have: non-empty weekdays list.
   *
   * @param reminder The reminder to check
   * @return true if reminder has weekday properties
   */
  private fun hasWeekdayProperties(reminder: Reminder): Boolean {
    return reminder.weekdays.isNotEmpty()
  }

  private fun hasAnyRepeatProperties(reminder: Reminder): Boolean {
    return hasTimerProperties(reminder) ||
      hasYearlyProperties(reminder) ||
      hasMonthlyProperties(reminder) ||
      hasWeekdayProperties(reminder) ||
      reminder.repeatInterval > 0 ||
      !reminder.recurDataObject.isNullOrEmpty()
  }
}

