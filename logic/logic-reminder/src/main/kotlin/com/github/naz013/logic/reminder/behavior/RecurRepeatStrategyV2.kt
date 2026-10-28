package com.github.naz013.logic.reminder.behavior

import com.github.naz013.logic.reminder.RecurEventManager
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.isEventInFuture
import com.github.naz013.logic.reminder.reminderUtcToLocal
import org.threeten.bp.LocalDateTime

class RecurRepeatStrategyV2(
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
) : ReminderBehaviorStrategyV2 {
  override fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? {
    val rrule = (reminder.recurrence as? RecurrenceRule.ICalendar)?.rrule ?: return null
    return recurEventManager.getNextAfterDateTime(fromDateTime, rrule)
  }

  // Fixed-zone RRULE reminders are unsupported until 3b: canSkip uses the fixed zone, the RDATE list the device zone.
  override fun canSkip(reminder: ReminderV2): Boolean {
    val rrule = (reminder.recurrence as? RecurrenceRule.ICalendar)?.rrule?.takeIf { it.isNotEmpty() } ?: return false
    val currentEventTime = reminder.schedule.eventDateTime?.let { dateTimeManager.reminderUtcToLocal(reminder, it) }
    return recurEventManager.getNextAfterDateTime(currentEventTime, rrule) != null
  }

  override fun canSnooze(reminder: ReminderV2): Boolean = true

  override fun canStartImmediately(reminder: ReminderV2): Boolean =
    dateTimeManager.isEventInFuture(reminder)
}
