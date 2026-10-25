package com.github.naz013.logic.habit

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.habit.HabitFrequency
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import kotlin.math.ceil

/**
 * Builds the per-day grid behind the activity bar: [weeks] Monday-start weeks ending with the week of
 * today. Daily habits scale the level by count over target; weekly habits show a logged day as
 * partial, or full when its week's target is met.
 */
class HabitHistoryBuilder {

  fun build(habit: Habit, checkIns: List<HabitCheckIn>, today: LocalDate, weeks: Int): List<HabitDay> {
    if (weeks <= 0) return emptyList()
    val target = habit.targetCount.coerceAtLeast(1)
    val countByDay = checkIns.groupBy { it.date }.mapValues { (_, items) -> items.sumOf { it.count } }
    val countByWeek = checkIns.groupBy { it.date.weekStart() }.mapValues { (_, items) -> items.sumOf { it.count } }
    val first = today.weekStart().minusWeeks(weeks - 1L)
    return generateSequence(first) { it.plusDays(1) }
      .takeWhile { !it.isAfter(today) }
      .map { date ->
        val count = countByDay[date] ?: 0
        val level = when (habit.frequency) {
          HabitFrequency.DAILY -> dailyLevel(count, target)
          HabitFrequency.WEEKLY -> weeklyLevel(count, countByWeek[date.weekStart()] ?: 0, target)
        }
        HabitDay(date = date, count = count, level = level)
      }
      .toList()
  }

  private fun dailyLevel(count: Int, target: Int): Int {
    if (count <= 0) return 0
    return ceil(HabitDay.MAX_LEVEL * count.toDouble() / target).toInt().coerceIn(1, HabitDay.MAX_LEVEL)
  }

  private fun weeklyLevel(count: Int, weekCount: Int, target: Int): Int {
    return when {
      count <= 0 -> 0
      weekCount >= target -> HabitDay.MAX_LEVEL
      else -> HabitDay.MAX_LEVEL / 2
    }
  }

  private fun LocalDate.weekStart(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
