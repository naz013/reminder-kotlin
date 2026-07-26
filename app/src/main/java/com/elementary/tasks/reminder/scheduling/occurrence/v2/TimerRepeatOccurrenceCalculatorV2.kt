package com.elementary.tasks.reminder.scheduling.occurrence.v2

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.datecalc.RecurrenceCalculatorImpl
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.repeatLimitOrDefault
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

/**
 * `ReminderV2`-typed mirror of [com.elementary.tasks.reminder.scheduling.occurrence.TimerRepeatOccurrenceCalculator].
 * Covers repeating [RecurrenceRule.Countdown] reminders that also have a quiet-hours exclusion
 * window set - see [com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2]
 * for the exact routing signal.
 */
class TimerRepeatOccurrenceCalculatorV2(
  private val dateTimeManager: DateTimeManager,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculatorImpl(),
) : ReminderOccurrenceCalculatorV2 {
  override suspend fun calculateOccurrences(
    reminder: ReminderV2,
    fromDateTime: LocalDateTime,
    numberOfOccurrences: Int,
  ): List<LocalDateTime> {
    if (numberOfOccurrences <= 0) {
      Logger.w(TAG, "calculateOccurrences: numberOfOccurrences must be positive")
      return emptyList()
    }

    val countdown = reminder.recurrence as? RecurrenceRule.Countdown
    if (countdown == null || countdown.repeatInterval <= 0L) {
      Logger.w(TAG, "calculateOccurrences: reminder repeatInterval must be positive")
      return emptyList()
    }

    val remainingLimit =
      if (reminder.isLimited()) {
        maxOf(reminder.recurrence.repeatLimitOrDefault() - reminder.eventCount.toInt(), 0)
      } else {
        Int.MAX_VALUE
      }

    val maxOccurrences = minOf(numberOfOccurrences, remainingLimit)

    if (maxOccurrences <= 0) {
      Logger.d(TAG, "calculateOccurrences: limit already exceeded")
      return emptyList()
    }

    val notification = reminder.notification
    val fromTime = dateTimeManager.toLocalTime(notification.quietHoursFrom)
    val toTime = dateTimeManager.toLocalTime(notification.quietHoursTo)

    val occurrences = mutableListOf<LocalDateTime>()
    var startDateTime = fromDateTime

    repeat(maxOccurrences) {
      val nextOccurrence =
        recurrenceCalculator.getNextTimerDateTime(
          eventDateTime = startDateTime,
          interval = countdown.repeatInterval,
          excludedHours = notification.activeHours.orEmpty(),
          excludedFromTime = fromTime,
          excludedToTime = toTime,
        )

      occurrences.add(nextOccurrence)
      startDateTime = nextOccurrence
    }

    Logger.d(TAG, "calculateOccurrences: generated ${occurrences.size} occurrences")
    return occurrences
  }

  companion object {
    private const val TAG = "TimerRepeatOccurrCalcV2"
  }
}
