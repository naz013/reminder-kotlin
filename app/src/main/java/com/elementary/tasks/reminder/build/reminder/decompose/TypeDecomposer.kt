package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.reminder.build.BuilderItem
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2

class TypeDecomposer(
  private val byDateDecomposer: ByDateDecomposer,
  private val byTimerDecomposer: ByTimerDecomposer,
  private val byWeekdaysDecomposer: ByWeekdaysDecomposer,
  private val byDayOfMonthDecomposer: ByDayOfMonthDecomposer,
  private val byDayOfYearDecomposer: ByDayOfYearDecomposer,
  private val byLocationDecomposer: ByLocationDecomposer,
  private val iCalDecomposer: ICalDecomposer,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    return when (reminder.recurrence) {
      RecurrenceRule.Once, is RecurrenceRule.Daily -> byDateDecomposer(reminder)
      is RecurrenceRule.Countdown -> byTimerDecomposer(reminder)
      is RecurrenceRule.Weekly -> byWeekdaysDecomposer(reminder)
      is RecurrenceRule.Monthly -> byDayOfMonthDecomposer(reminder)
      // No builder field support yet for "nth weekday of month" - unreachable today; nothing in
      // the composer (RecurrenceRuleCalculator) produces this recurrence shape yet either.
      is RecurrenceRule.RelativeMonthly -> emptyList()
      is RecurrenceRule.Yearly -> byDayOfYearDecomposer(reminder)
      RecurrenceRule.LocationEnter, RecurrenceRule.LocationExit -> byLocationDecomposer(reminder)
      is RecurrenceRule.ICalendar -> iCalDecomposer(reminder)
    }
  }
}
