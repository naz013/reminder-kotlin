package com.github.naz013.logic.reminder.behavior

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.RecurEventManager

/**
 * Resolves the scheduling behavior strategy for a [com.github.naz013.domain.reminder.v2.ReminderV2].
 *
 * Switches on [com.github.naz013.domain.reminder.v2.ReminderV2.recurrence] (the sealed `RecurrenceRule`) rather than raw-field
 * heuristics, EXCEPT where the underlying signal is not actually type-derived to begin with (the
 * location/shopping/timer-exclusion checks below) - those are kept as raw-field reads since
 * `recurrence` alone would not distinguish what's needed there.
 *
 * Priority order (matches V1 exactly):
 * 1. Location-based, but with a current delayed event time (if has places + event time + current) - SimpleDate
 * 2. Location-based (if has places) - Location
 * 3. Shopping list (if has shopping items without an event time or any repeat properties) - NoReminder
 * 4. Recurrence (if has an RRULE) - Recur
 * 5. Timer-exclusion (Countdown with a quiet-hours window set) - Timer
 * 6. Weekly (if Weekly with weekdays) - Weekday
 * 7. Yearly - Yearly
 * 8. Monthly - Monthly
 * 9. Interval repeat (Daily or exclusion-less Countdown with repeatInterval > 0) - Interval
 * 10. Simple date (default)
 */
class BehaviorStrategyResolverV2(
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
  private val recurrenceCalculator: RecurrenceCalculator,
) {
  fun resolve(reminder: ReminderV2): ReminderBehaviorStrategyV2 =
    when {
      reminder.places.isNotEmpty() && hasCurrentEventTime(reminder) -> {
        SimpleDateStrategyV2(dateTimeManager)
      }

      reminder.places.isNotEmpty() -> {
        LocationBasedStrategyV2
      }

      reminder.shoppingItems.isNotEmpty() && !hasEventTime(reminder) && !hasAnyRepeatProperties(reminder) -> {
        NoReminderStrategyV2
      }

      hasRecurProperties(reminder) -> {
        RecurRepeatStrategyV2(dateTimeManager, recurEventManager)
      }

      hasTimerExclusionProperties(reminder) -> {
        TimerRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
      }

      hasWeekdayProperties(reminder) -> {
        WeekdayRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
      }

      hasYearlyProperties(reminder) -> {
        YearlyRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
      }

      hasMonthlyProperties(reminder) -> {
        MonthlyRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
      }

      repeatInterval(reminder) > 0 -> {
        IntervalRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
      }

      else -> {
        SimpleDateStrategyV2(dateTimeManager)
      }
    }

  private fun hasEventTime(reminder: ReminderV2): Boolean = reminder.schedule.eventDateTime != null

  private fun hasCurrentEventTime(reminder: ReminderV2): Boolean =
    reminder.schedule.eventDateTime?.let { dateTimeManager.isCurrent(dateTimeManager.utcToLocal(it)) } ?: false

  private fun hasRecurProperties(reminder: ReminderV2): Boolean =
    (reminder.recurrence as? RecurrenceRule.ICalendar)?.rrule?.isNotEmpty() == true

  /** Matches V1's `hasTimerProperties`: a quiet-hours exclusion window set AND a nonzero [RecurrenceRule.Countdown.after].
   * Deliberately not derived from [ReminderV2.recurrence] alone - see the class doc. */
  private fun hasTimerExclusionProperties(reminder: ReminderV2): Boolean {
    val notification = reminder.notification
    val hasExclusionWindow =
      !notification.quietHoursFrom.isNullOrEmpty() ||
        !notification.quietHoursTo.isNullOrEmpty() ||
        !notification.activeHours.isNullOrEmpty()
    val countdown = reminder.recurrence as? RecurrenceRule.Countdown ?: return false
    return hasExclusionWindow && countdown.after != 0L
  }

  private fun hasYearlyProperties(reminder: ReminderV2): Boolean = reminder.recurrence is RecurrenceRule.Yearly

  private fun hasMonthlyProperties(reminder: ReminderV2): Boolean = reminder.recurrence is RecurrenceRule.Monthly

  private fun hasWeekdayProperties(reminder: ReminderV2): Boolean =
    (reminder.recurrence as? RecurrenceRule.Weekly)?.weekdays?.isNotEmpty() == true

  private fun repeatInterval(reminder: ReminderV2): Long =
    when (val recurrence = reminder.recurrence) {
      is RecurrenceRule.Daily -> recurrence.repeatInterval
      is RecurrenceRule.Countdown -> recurrence.repeatInterval
      else -> 0L
    }

  private fun hasAnyRepeatProperties(reminder: ReminderV2): Boolean =
    hasRecurProperties(reminder) ||
      hasTimerExclusionProperties(reminder) ||
      hasYearlyProperties(reminder) ||
      hasMonthlyProperties(reminder) ||
      hasWeekdayProperties(reminder) ||
      repeatInterval(reminder) > 0
}
