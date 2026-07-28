package com.elementary.tasks.reminder.scheduling.behavior.v2

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.datecalc.RecurrenceCalculatorImpl
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.behavior.YearlyRepeatStrategy].
 * Strategy for [RecurrenceRule.Yearly] reminders.
 */
class YearlyRepeatStrategyV2(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculatorImpl(),
) : ReminderBehaviorStrategyV2 {
  override fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    val yearly = reminder.recurrence as? RecurrenceRule.Yearly ?: return null
    val eventDateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return null

    return recurrenceCalculator.findNextYearDayDateTime(
      eventDateTime = eventDateTime,
      dayOfMonth = yearly.dayOfMonth,
      monthOfYear = yearly.monthOfYear,
      interval = yearly.repeatInterval,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
    )
  }

  override fun canSkip(reminder: ReminderV2): Boolean = !reminder.isLimitExceed()

  override fun canSnooze(reminder: ReminderV2): Boolean = true

  override fun canStartImmediately(reminder: ReminderV2): Boolean =
    reminder.schedule.eventDateTime?.let { dateTimeManager.isCurrent(dateTimeManager.utcToLocal(it)) } ?: false
}
