package com.github.naz013.logic.reminder.behavior

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.isEventInFuture
import com.github.naz013.logic.reminder.reminderNow
import com.github.naz013.logic.reminder.reminderUtcToLocal
import org.threeten.bp.LocalDateTime

class YearlyRepeatStrategyV2(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator,
) : ReminderBehaviorStrategyV2 {
  override fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    val yearly = reminder.recurrence as? RecurrenceRule.Yearly ?: return null
    val eventDateTime = reminder.schedule.eventDateTime
      ?.let { dateTimeManager.reminderUtcToLocal(reminder, it) }
      ?: return null

    return recurrenceCalculator.findNextYearDayDateTime(
      eventDateTime = eventDateTime,
      dayOfMonth = yearly.dayOfMonth,
      monthOfYear = yearly.monthOfYear,
      interval = yearly.repeatInterval,
      afterOrEqualDateTime = dateTimeManager.reminderNow(reminder),
    )
  }

  override fun canSkip(reminder: ReminderV2): Boolean = !reminder.isLimitExceed()

  override fun canSnooze(reminder: ReminderV2): Boolean = true

  override fun canStartImmediately(reminder: ReminderV2): Boolean =
    dateTimeManager.isEventInFuture(reminder)
}
