package com.github.naz013.logic.habit

import com.github.naz013.domain.habit.Habit

/**
 * [periodCount] is today's count for daily habits and this ISO week's total for weekly ones;
 * [todayCount] is always today's own count.
 */
data class HabitWithProgress(
  val habit: Habit,
  val todayCount: Int,
  val periodCount: Int,
  val streak: Int
) {
  val isPeriodTargetReached: Boolean
    get() = periodCount >= habit.targetCount
}
