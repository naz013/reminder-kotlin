package com.github.naz013.logic.habit

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.habit.HabitFrequency
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters

/**
 * Daily habits streak over consecutive days whose count reaches the target; weekly habits over
 * consecutive ISO weeks (Monday start) whose summed count reaches the target. The current,
 * still-unfinished day or week never breaks a streak - it only extends it once the target is met.
 */
class HabitStreakCalculator {

  fun streak(habit: Habit, checkIns: List<HabitCheckIn>, today: LocalDate): Int {
    val target = habit.targetCount.coerceAtLeast(1)
    return when (habit.frequency) {
      HabitFrequency.DAILY -> dailyStreak(target, checkIns, today)
      HabitFrequency.WEEKLY -> weeklyStreak(target, checkIns, today)
    }
  }

  fun periodCount(habit: Habit, checkIns: List<HabitCheckIn>, today: LocalDate): Int {
    return when (habit.frequency) {
      HabitFrequency.DAILY -> checkIns.filter { it.date == today }.sumOf { it.count }
      HabitFrequency.WEEKLY -> {
        val weekStart = today.weekStart()
        checkIns.filter { it.date.weekStart() == weekStart }.sumOf { it.count }
      }
    }
  }

  private fun dailyStreak(target: Int, checkIns: List<HabitCheckIn>, today: LocalDate): Int {
    val countByDay = checkIns.groupBy { it.date }.mapValues { (_, items) -> items.sumOf { it.count } }
    return countConsecutive(countByDay, target, today, step = 1L)
  }

  private fun weeklyStreak(target: Int, checkIns: List<HabitCheckIn>, today: LocalDate): Int {
    val countByWeek = checkIns.groupBy { it.date.weekStart() }.mapValues { (_, items) -> items.sumOf { it.count } }
    return countConsecutive(countByWeek, target, today.weekStart(), step = 7L)
  }

  private fun countConsecutive(
    countByPeriod: Map<LocalDate, Int>,
    target: Int,
    currentPeriod: LocalDate,
    step: Long
  ): Int {
    var cursor = if ((countByPeriod[currentPeriod] ?: 0) >= target) currentPeriod else currentPeriod.minusDays(step)
    var streak = 0
    while ((countByPeriod[cursor] ?: 0) >= target) {
      streak++
      cursor = cursor.minusDays(step)
    }
    return streak
  }

  private fun LocalDate.weekStart(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
