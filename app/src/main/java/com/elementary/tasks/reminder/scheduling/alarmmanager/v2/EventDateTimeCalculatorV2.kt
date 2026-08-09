package com.elementary.tasks.reminder.scheduling.alarmmanager.v2

import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.TimerRepeatStrategyV2
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.minusMillis
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.alarmmanager.EventDateTimeCalculator].
 * Not yet wired to any production call site (Phase C sub-phase C1) - see the migration plan.
 *
 * Simpler than the V1 version: [ReminderV2.schedule]'s `eventDateTime` is already a typed,
 * always-valid `LocalDateTime` (UTC-zoned), so there is no GMT-string parse-failure path.
 */
class EventDateTimeCalculatorV2(
  private val strategyResolver: BehaviorStrategyResolverV2,
  private val dateTimeManager: DateTimeManager,
) {
  fun calculateEventDateTime(reminder: ReminderV2): Long? {
    val eventDateTime = reminder.schedule.eventDateTime ?: run {
      Logger.w(TAG, "No event time set for reminder id=${reminder.uuId}")
      return null
    }
    var due = dateTimeManager.utcToLocal(eventDateTime)
    val remindBefore = reminder.notification.remindBefore
    if (remindBefore != null && remindBefore != 0L) {
      due = due.minusMillis(remindBefore)
    }
    val strategy = strategyResolver.resolve(reminder)
    if (strategy !is TimerRepeatStrategyV2) {
      due = due.withSecond(0)
    }
    Logger.i(TAG, "Calculated event time: ${dateTimeManager.logDateTime(due)}")
    val millis = dateTimeManager.toMillis(due)
    val nowMillis = dateTimeManager.toMillis(dateTimeManager.getCurrentDateTime())
    if (millis < nowMillis - PAST_TOLERANCE_MILLIS) {
      Logger.w(TAG, "Calculated event time is in the past: $due")
      return null
    }
    return millis
  }

  companion object {
    private const val TAG = "EventDateTimeCalculatorV2"
    private const val PAST_TOLERANCE_MILLIS = 60_000L
  }
}
