package com.elementary.tasks.reminder.scheduling.occurrence

import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.reminder.scheduling.behavior.IntervalRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.LocationBasedStrategy
import com.elementary.tasks.reminder.scheduling.behavior.MonthlyRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.NoReminderStrategy
import com.elementary.tasks.reminder.scheduling.behavior.RecurRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.ReminderBehaviorStrategy
import com.elementary.tasks.reminder.scheduling.behavior.SimpleDateStrategy
import com.elementary.tasks.reminder.scheduling.behavior.TimerRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.WeekdayRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.YearlyRepeatStrategy
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.logging.Logger

/**
 * Factory for creating ReminderOccurrenceCalculator instances based on
 * the reminder's behavior strategy.
 *
 * This factory maps each ReminderBehaviorStrategy to its corresponding
 * occurrence calculator implementation.
 */
class ReminderOccurrenceCalculatorFactory(
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
) {

  /**
   * Creates the appropriate ReminderOccurrenceCalculator for a given strategy.
   *
   * @param strategy The behavior strategy to get calculator for
   * @return The corresponding calculator implementation
   */
  fun createCalculator(strategy: ReminderBehaviorStrategy): ReminderOccurrenceCalculator {
    return when (strategy) {
      is SimpleDateStrategy -> {
        Logger.d(TAG, "createCalculator: SimpleDateStrategy -> SimpleDateOccurrenceCalculator")
        SimpleDateOccurrenceCalculator()
      }

      is IntervalRepeatStrategy -> {
        Logger.d(TAG, "createCalculator: IntervalRepeatStrategy -> IntervalRepeatOccurrenceCalculator")
        IntervalRepeatOccurrenceCalculator()
      }

      is WeekdayRepeatStrategy -> {
        Logger.d(TAG, "createCalculator: WeekdayRepeatStrategy -> WeekdayRepeatOccurrenceCalculator")
        WeekdayRepeatOccurrenceCalculator()
      }

      is MonthlyRepeatStrategy -> {
        Logger.d(TAG, "createCalculator: MonthlyRepeatStrategy -> MonthlyRepeatOccurrenceCalculator")
        MonthlyRepeatOccurrenceCalculator()
      }

      is YearlyRepeatStrategy -> {
        Logger.d(TAG, "createCalculator: YearlyRepeatStrategy -> YearlyRepeatOccurrenceCalculator")
        YearlyRepeatOccurrenceCalculator()
      }

      is TimerRepeatStrategy -> {
        Logger.d(TAG, "createCalculator: TimerRepeatStrategy -> TimerRepeatOccurrenceCalculator")
        TimerRepeatOccurrenceCalculator(dateTimeManager)
      }

      is RecurRepeatStrategy -> {
        Logger.d(TAG, "createCalculator: RecurRepeatStrategy -> RecurRepeatOccurrenceCalculator")
        RecurRepeatOccurrenceCalculator(recurEventManager)
      }

      is LocationBasedStrategy -> {
        Logger.d(TAG, "createCalculator: LocationBasedStrategy -> LocationBasedOccurrenceCalculator")
        LocationBasedOccurrenceCalculator()
      }

      is NoReminderStrategy -> {
        Logger.d(TAG, "createCalculator: NoReminderStrategy -> NoReminderOccurrenceCalculator")
        NoReminderOccurrenceCalculator()
      }

      else -> {
        Logger.w(TAG, "createCalculator: Unknown strategy type ${strategy::class.simpleName}, using NoReminderOccurrenceCalculator")
        NoReminderOccurrenceCalculator()
      }
    }
  }

  companion object {
    private const val TAG = "OccurrenceCalcFactory"
  }
}

