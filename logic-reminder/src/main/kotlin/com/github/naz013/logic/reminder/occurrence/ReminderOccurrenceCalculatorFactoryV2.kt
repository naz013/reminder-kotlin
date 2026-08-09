package com.github.naz013.logic.reminder.occurrence

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.RecurEventManager
import com.github.naz013.logic.reminder.behavior.IntervalRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.LocationBasedStrategyV2
import com.github.naz013.logic.reminder.behavior.MonthlyRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.NoReminderStrategyV2
import com.github.naz013.logic.reminder.behavior.RecurRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.ReminderBehaviorStrategyV2
import com.github.naz013.logic.reminder.behavior.SimpleDateStrategyV2
import com.github.naz013.logic.reminder.behavior.TimerRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.WeekdayRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.YearlyRepeatStrategyV2

class ReminderOccurrenceCalculatorFactoryV2(
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
  private val recurrenceCalculator: RecurrenceCalculator,
) {
  fun createCalculator(strategy: ReminderBehaviorStrategyV2): ReminderOccurrenceCalculatorV2 =
    when (strategy) {
      is SimpleDateStrategyV2 -> {
        Logger.d(TAG, "createCalculator: SimpleDateStrategyV2 -> SimpleDateOccurrenceCalculatorV2")
        SimpleDateOccurrenceCalculatorV2()
      }

      is IntervalRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: IntervalRepeatStrategyV2 -> IntervalRepeatOccurrenceCalculatorV2")
        IntervalRepeatOccurrenceCalculatorV2(recurrenceCalculator)
      }

      is WeekdayRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: WeekdayRepeatStrategyV2 -> WeekdayRepeatOccurrenceCalculatorV2")
        WeekdayRepeatOccurrenceCalculatorV2(recurrenceCalculator)
      }

      is MonthlyRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: MonthlyRepeatStrategyV2 -> MonthlyRepeatOccurrenceCalculatorV2")
        MonthlyRepeatOccurrenceCalculatorV2(recurrenceCalculator)
      }

      is YearlyRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: YearlyRepeatStrategyV2 -> YearlyRepeatOccurrenceCalculatorV2")
        YearlyRepeatOccurrenceCalculatorV2(recurrenceCalculator)
      }

      is TimerRepeatStrategyV2 -> {
        Logger.d(TAG, "createCalculator: TimerRepeatStrategyV2 -> TimerRepeatOccurrenceCalculatorV2")
        TimerRepeatOccurrenceCalculatorV2(dateTimeManager, recurrenceCalculator)
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
