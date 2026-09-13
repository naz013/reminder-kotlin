package com.github.naz013.insights.aggregator

import com.github.naz013.domain.pomodoro.PomodoroSessionRecord
import org.threeten.bp.LocalDate

/**
 * Unlike [ReminderStreakCalculator]/[RoutineStreakCalculator], Pomodoro focus has no per-entity
 * grouping key - there's one streak for "did the user complete at least one focus session that
 * day" across the whole feature, not per reminder.
 */
internal object PomodoroFocusCalculator {

  data class FocusSummary(
    val totalFocusSeconds: Int,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
  )

  fun calculate(records: List<PomodoroSessionRecord>, today: LocalDate): FocusSummary {
    val completedDates = records
      .asSequence()
      .filter { it.wasCompleted }
      .map { it.startedAt.toLocalDate() }
      .distinct()
      .sorted()
      .toList()
    return FocusSummary(
      totalFocusSeconds = records.sumOf { it.actualFocusSeconds },
      currentStreakDays = currentStreak(completedDates, today),
      longestStreakDays = longestStreak(completedDates),
    )
  }

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
