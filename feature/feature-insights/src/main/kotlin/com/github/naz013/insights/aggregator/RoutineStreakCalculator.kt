package com.github.naz013.insights.aggregator

import com.github.naz013.domain.routine.RoutineExecutionRecord
import org.threeten.bp.LocalDate

/**
 * A "streak day" for a routine is a day with at least one [RoutineExecutionRecord] where every
 * step was completed (`completedStepsCount == totalStepsCount`) - unlike reminders (which have no
 * completed/not-completed concept and count any firing, see [ReminderStreakCalculator]), a
 * recurring routine auto-writes a zero-completion record every day it's due whether or not the
 * user actually did it (see `RoutineRecurrenceResetUseCase`), so counting *any* record here would
 * make every recurring routine look like a perfect streak regardless of real adherence.
 */
internal object RoutineStreakCalculator {

  data class Streak(
    val routineId: String,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val lastCompletedDate: LocalDate?
  )

  fun calculate(records: List<RoutineExecutionRecord>, today: LocalDate): List<Streak> {
    return records
      .groupBy { it.routineId }
      .map { (routineId, recordsForRoutine) -> calculateForRoutine(routineId, recordsForRoutine, today) }
  }

  private fun calculateForRoutine(
    routineId: String,
    records: List<RoutineExecutionRecord>,
    today: LocalDate
  ): Streak {
    val completedDates = records
      .asSequence()
      .filter { it.totalStepsCount > 0 && it.completedStepsCount == it.totalStepsCount }
      .map { it.executedAt.toLocalDate() }
      .distinct()
      .sorted()
      .toList()
    return Streak(
      routineId = routineId,
      currentStreakDays = currentStreak(completedDates, today),
      longestStreakDays = longestStreak(completedDates),
      lastCompletedDate = completedDates.lastOrNull()
    )
  }

  /** Counts backward from today; if the routine hasn't been fully completed yet today, starts
   * from yesterday instead so an in-progress streak isn't reported as broken before the day is
   * even over. */
  private fun currentStreak(sortedDistinctDates: List<LocalDate>, today: LocalDate): Int {
    val dateSet = sortedDistinctDates.toHashSet()
    var cursor = if (dateSet.contains(today)) today else today.minusDays(1)
    var streak = 0
    while (dateSet.contains(cursor)) {
      streak++
      cursor = cursor.minusDays(1)
    }
    return streak
  }

  private fun longestStreak(sortedDistinctDates: List<LocalDate>): Int {
    if (sortedDistinctDates.isEmpty()) return 0
    var longest = 1
    var current = 1
    for (i in 1 until sortedDistinctDates.size) {
      current = if (sortedDistinctDates[i - 1].plusDays(1) == sortedDistinctDates[i]) current + 1 else 1
      longest = maxOf(longest, current)
    }
    return longest
  }
}
