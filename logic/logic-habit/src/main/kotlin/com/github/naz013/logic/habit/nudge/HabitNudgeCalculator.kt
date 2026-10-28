package com.github.naz013.logic.habit.nudge

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.HabitWithProgress
import org.threeten.bp.LocalDateTime

class HabitNudgeCalculator {

  /** Next local moment after [now] at the habit's reminder time; weekly habits are nudged daily too. */
  fun nextTrigger(habit: Habit, now: LocalDateTime): LocalDateTime? {
    val time = habit.reminderTime ?: return null
    val today = LocalDateTime.of(now.toLocalDate(), time)
    return if (today.isAfter(now)) today else today.plusDays(1)
  }

  fun shouldNotify(progress: HabitWithProgress): Boolean {
    return !progress.habit.remindOnlyIfNotDone || !isDone(progress)
  }

  private fun isDone(progress: HabitWithProgress): Boolean {
    return when (progress.habit.frequency) {
      HabitFrequency.DAILY -> progress.isPeriodTargetReached
      HabitFrequency.WEEKLY -> progress.isPeriodTargetReached || progress.todayCount > 0
    }
  }
}
