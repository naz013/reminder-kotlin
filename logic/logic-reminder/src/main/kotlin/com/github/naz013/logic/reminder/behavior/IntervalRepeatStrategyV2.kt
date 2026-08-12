package com.github.naz013.logic.reminder.behavior

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

class IntervalRepeatStrategyV2(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator,
) : ReminderBehaviorStrategyV2 {

  override fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    val repeatInterval = repeatInterval(reminder)
    if (repeatInterval <= 0L) return null
    val eventDateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return null

    return recurrenceCalculator.findNextIntervalDateTime(
      eventDateTime = eventDateTime,
      intervalMillis = repeatInterval,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
    )
  }

  override fun canSkip(reminder: ReminderV2): Boolean {
    val repeatInterval = repeatInterval(reminder)
    Logger.v(
      TAG,
      "CanSkip called for reminder id=${reminder.uuId}, " +
        "repeatInterval=$repeatInterval, isLimitExceed=${reminder.isLimitExceed()}",
    )
    return repeatInterval > 0 && !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: ReminderV2): Boolean = true

  override fun canStartImmediately(reminder: ReminderV2): Boolean =
    reminder.schedule.eventDateTime?.let { dateTimeManager.isCurrent(dateTimeManager.utcToLocal(it)) } ?: false

  private fun repeatInterval(reminder: ReminderV2): Long =
    when (val recurrence = reminder.recurrence) {
      is RecurrenceRule.Daily -> recurrence.repeatInterval
      is RecurrenceRule.Countdown -> recurrence.repeatInterval
      else -> 0L
    }

  companion object {
    private const val TAG = "IntervalRepeatStrategyV2"
  }
}
