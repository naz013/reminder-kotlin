package com.github.naz013.logic.habit

import com.github.naz013.domain.habit.Habit

data class HabitDetails(
  val habit: Habit,
  val todayCount: Int,
  val periodCount: Int,
  val streak: Int,
  val longestStreak: Int,
  val totalCheckIns: Int,
  /** Whole Monday-start weeks, oldest first, ending with the week of today (future days omitted). */
  val days: List<HabitDay>
) {
  val progress: HabitWithProgress
    get() = HabitWithProgress(habit, todayCount, periodCount, streak)
}
