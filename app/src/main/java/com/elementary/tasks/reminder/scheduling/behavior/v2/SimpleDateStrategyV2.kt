package com.elementary.tasks.reminder.scheduling.behavior.v2

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.behavior.SimpleDateStrategy].
 * Strategy for simple one-time date/time reminders. No repeat pattern - fires once at the specified time.
 */
class SimpleDateStrategyV2(
  private val dateTimeManager: DateTimeManager,
) : ReminderBehaviorStrategyV2 {
  override fun calculateNextOccurrence(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
  ): LocalDateTime? = null

  override fun canSkip(reminder: ReminderV2): Boolean = false

  override fun canSnooze(reminder: ReminderV2): Boolean = true

  override fun canStartImmediately(reminder: ReminderV2): Boolean =
    reminder.schedule.eventDateTime?.let { dateTimeManager.isCurrent(dateTimeManager.utcToLocal(it)) } ?: false
}
