package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.reminder.scheduling.behavior.v2.IntervalRepeatStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.LocationBasedStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.MonthlyRepeatStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.NoReminderStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.RecurRepeatStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.ReminderBehaviorStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.SimpleDateStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.TimerRepeatStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.WeekdayRepeatStrategyV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.YearlyRepeatStrategyV2
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.logging.Logger

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.occurrence.ReminderOccurrenceCalculatorFactory].
 * Maps each [ReminderBehaviorStrategyV2] to its corresponding occurrence calculator implementation.
 */
class ReminderOccurrenceCalculatorFactoryV2(
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
) {
  fun createCalculator(strategy: ReminderBehaviorStrategyV2): ReminderOccurrenceCalculatorV2 =
    when (strategy) {
      is SimpleDateStrategyV2 -> {
        Logger.d(TAG, "createCalculator: SimpleDateStrategyV2 -> SimpleDateOccurrenceCalculatorV2")
        SimpleDateOccurrenceCalculatorV2()
      }

      is IntervalRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: IntervalRepeatStrategyV2 -> IntervalRepeatOccurrenceCalculatorV2")
        IntervalRepeatOccurrenceCalculatorV2()
      }

      is WeekdayRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: WeekdayRepeatStrategyV2 -> WeekdayRepeatOccurrenceCalculatorV2")
        WeekdayRepeatOccurrenceCalculatorV2()
      }

      is MonthlyRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: MonthlyRepeatStrategyV2 -> MonthlyRepeatOccurrenceCalculatorV2")
        MonthlyRepeatOccurrenceCalculatorV2()
      }

      is YearlyRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: YearlyRepeatStrategyV2 -> YearlyRepeatOccurrenceCalculatorV2")
        YearlyRepeatOccurrenceCalculatorV2()
      }

      is TimerRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: TimerRepeatStrategyV2 -> TimerRepeatOccurrenceCalculatorV2")
        TimerRepeatOccurrenceCalculatorV2(dateTimeManager)
      }

      is RecurRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: RecurRepeatStrategyV2 -> RecurRepeatOccurrenceCalculatorV2")
        RecurRepeatOccurrenceCalculatorV2(recurEventManager)
      }

      is LocationBasedStrategyV2 -> {
        Logger.d(TAG, "createCalculator: LocationBasedStrategyV2 -> LocationBasedOccurrenceCalculatorV2")
        LocationBasedOccurrenceCalculatorV2()
      }

      is NoReminderStrategyV2 -> {
        Logger.d(TAG, "createCalculator: NoReminderStrategyV2 -> NoReminderOccurrenceCalculatorV2")
        NoReminderOccurrenceCalculatorV2()
      }

      else -> {
        Logger.w(TAG, "createCalculator: Unknown strategy type ${strategy::class.simpleName}, using NoReminderOccurrenceCalculatorV2")
        NoReminderOccurrenceCalculatorV2()
      }
    }

  companion object {
    private const val TAG = "OccurrenceCalcFactoryV2"
  }
}
