package com.elementary.tasks.reminder.scheduling.behavior.v2

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.datecalc.RecurrenceCalculatorImpl
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.behavior.TimerRepeatStrategy].
 * Strategy for repeating [RecurrenceRule.Countdown] reminders that also have a quiet-hours
 * exclusion window set ([com.github.naz013.domain.reminder.v2.NotificationSettingsOverride.activeHours]/
 * `quietHoursFrom`/`quietHoursTo`) - see [BehaviorStrategyResolverV2] for the exact routing signal.
 */
class TimerRepeatStrategyV2(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculatorImpl(),
) : ReminderBehaviorStrategyV2 {
  override fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? {
    if (reminder.isLimitExceed()) return null
    val countdown = reminder.recurrence as? RecurrenceRule.Countdown ?: return null
    if (countdown.repeatInterval <= 0L) return null
    val eventDateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return null
    val notification = reminder.notification
    val fromTime = dateTimeManager.toLocalTime(notification.quietHoursFrom)
    val toTime = dateTimeManager.toLocalTime(notification.quietHoursTo)
    return recurrenceCalculator.findNextTimerDateTime(
      eventDateTime = eventDateTime,
      interval = countdown.repeatInterval,
      excludedHours = notification.activeHours.orEmpty(),
      excludedFromTime = fromTime,
      excludedToTime = toTime,
      afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
    )
  }

  override fun canSkip(reminder: ReminderV2): Boolean {
    val countdown = reminder.recurrence as? RecurrenceRule.Countdown ?: return false
    return countdown.repeatInterval > 0L && !reminder.isLimitExceed()
  }

  override fun canSnooze(reminder: ReminderV2): Boolean = true

  override fun canStartImmediately(reminder: ReminderV2): Boolean =
    reminder.schedule.eventDateTime?.let { dateTimeManager.isCurrent(dateTimeManager.utcToLocal(it)) } ?: false
}
