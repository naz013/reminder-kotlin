package com.github.naz013.logic.reminder.occurrence

import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.repeatLimitOrDefault
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

class IntervalRepeatOccurrenceCalculatorV2(
  private val recurrenceCalculator: RecurrenceCalculator,
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

    val repeatInterval =
      when (val recurrence = reminder.recurrence) {
        is RecurrenceRule.Daily -> recurrence.repeatInterval
        is RecurrenceRule.Countdown -> recurrence.repeatInterval
        else -> 0L
      }
    if (repeatInterval <= 0) {
      Logger.w(TAG, "calculateOccurrences: repeatInterval must be positive")
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

    val occurrences = mutableListOf<LocalDateTime>()
    var startDateTime = fromDateTime

    repeat(maxOccurrences) {
      val nextOccurrence =
        recurrenceCalculator.getNextIntervalDateTime(
          startDateTime,
          repeatInterval,
        )

      occurrences.add(nextOccurrence)
      startDateTime = nextOccurrence
    }

    Logger.d(TAG, "calculateOccurrences: generated ${occurrences.size} occurrences")
    return occurrences
  }

  companion object {
    private const val TAG = "IntervalRepeatOccurrCalcV2"
  }
}
