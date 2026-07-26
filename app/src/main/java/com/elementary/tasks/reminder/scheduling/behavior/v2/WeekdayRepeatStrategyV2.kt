package com.elementary.tasks.reminder.scheduling.behavior.v2

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.datecalc.RecurrenceCalculatorImpl
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.behavior.WeekdayRepeatStrategy].
 * Strategy for [RecurrenceRule.Weekly] reminders.
 */
class WeekdayRepeatStrategyV2(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculatorImpl(),
) : ReminderBehaviorStrategyV2 {
  override fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    val weekly = reminder.recurrence as? RecurrenceRule.Weekly ?: return null
    if (weekly.weekdays.isEmpty()) return null
    val eventDateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return null

    return recurrenceCalculator.findNextDayOfWeekDateTime(
      eventDateTime = eventDateTime,
      weekdays = weekly.weekdays,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
    )
  }

  override fun canSkip(reminder: ReminderV2): Boolean {
    val weekly = reminder.recurrence as? RecurrenceRule.Weekly ?: return false
    return weekly.weekdays.isNotEmpty() && !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: ReminderV2): Boolean = true

  override fun canStartImmediately(reminder: ReminderV2): Boolean =
    reminder.schedule.eventDateTime?.let { dateTimeManager.isCurrent(dateTimeManager.utcToLocal(it)) } ?: false
}
