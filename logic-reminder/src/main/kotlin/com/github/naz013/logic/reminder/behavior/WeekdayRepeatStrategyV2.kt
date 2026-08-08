package com.github.naz013.logic.reminder.behavior

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

class WeekdayRepeatStrategyV2(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator,
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
