package com.github.naz013.logic.reminder.occurrence

import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.repeatLimitOrDefault
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

class YearlyRepeatOccurrenceCalculatorV2(
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

    val yearly = reminder.recurrence as? RecurrenceRule.Yearly
    if (yearly == null || yearly.dayOfMonth < 0 || yearly.monthOfYear < 0) {
      Logger.w(TAG, "calculateOccurrences: dayOfMonth and monthOfYear must be positive")
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
        recurrenceCalculator.getNextYearDayDateTime(
          eventDateTime = startDateTime,
          monthOfYear = yearly.monthOfYear,
          dayOfMonth = yearly.dayOfMonth,
          interval = yearly.repeatInterval,
        )

      occurrences.add(nextOccurrence)
      startDateTime = nextOccurrence
    }

    Logger.d(TAG, "calculateOccurrences: generated ${occurrences.size} occurrences")
    return occurrences
  }

  companion object {
    private const val TAG = "YearlyRepeatOccurrCalcV2"
  }
}
