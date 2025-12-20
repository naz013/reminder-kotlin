package com.elementary.tasks.reminder.scheduling.alarmmanager

import com.elementary.tasks.reminder.scheduling.behavior.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.behavior.TimerRepeatStrategy
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.datetime.minusMillis
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

class EventDateTimeCalculator(
  private val strategyResolver: BehaviorStrategyResolver,
  private val dateTimeManager: DateTimeManager,
) {

  fun calculateEventDateTime(reminder: Reminder): Long? {
    var due = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: run {
      Logger.w(TAG, "Cannot parse event time: ${reminder.eventTime}")
      return null
    }
    if (reminder.remindBefore != 0L) {
      due = due.minusMillis(reminder.remindBefore)
    }
    val strategy = strategyResolver.resolve(reminder)
    if (strategy !is TimerRepeatStrategy) {
      due = due.withSecond(0)
    }
    Logger.i(TAG, "Calculated event time: ${dateTimeManager.logDateTime(due)}")
    val millis = dateTimeManager.toMillis(due)
    if (millis <= 0) {
      Logger.w(TAG, "Calculated event time is in the past: $due")
      return null
    }
    return millis
  }

  companion object {
    private const val TAG = "EventDateTimeCalculator"
  }
}
