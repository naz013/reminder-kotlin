package com.github.naz013.logic.habit.usecase

import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.HabitWithProgress

/** One-tap check-in: clears or completes the day for single-step habits, otherwise adds one step. */
class CheckInHabitUseCase(
  private val toggleHabitCheckInUseCase: ToggleHabitCheckInUseCase,
  private val incrementHabitCheckInUseCase: IncrementHabitCheckInUseCase
) {
  suspend operator fun invoke(progress: HabitWithProgress) {
    val id = progress.habit.id
    if (shouldToggle(progress)) {
      toggleHabitCheckInUseCase(id)
    } else {
      incrementHabitCheckInUseCase(id)
    }
  }

  private fun shouldToggle(progress: HabitWithProgress): Boolean {
    val habit = progress.habit
    return when (habit.frequency) {
      HabitFrequency.DAILY -> habit.targetCount <= 1 || progress.todayCount >= habit.targetCount
      HabitFrequency.WEEKLY -> progress.todayCount > 0
    }
  }
}
