package com.github.naz013.appwidgets.habit

import com.github.naz013.logging.Logger
import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitsWithProgressUseCase
import com.github.naz013.logic.routine.HabitConfig
import kotlinx.coroutines.flow.first

internal class HabitCheckInHandler(
  private val habitConfig: HabitConfig,
  private val observeHabitsWithProgressUseCase: ObserveHabitsWithProgressUseCase,
  private val checkInHabitUseCase: CheckInHabitUseCase
) {

  suspend fun checkIn(habitId: String): Boolean {
    if (!habitConfig.isEnabled) {
      Logger.w(TAG, "Habits are not available, ignoring check-in")
      return false
    }
    val progress = observeHabitsWithProgressUseCase().first().firstOrNull { it.habit.id == habitId }
    if (progress == null) {
      Logger.w(TAG, "Habit not found: $habitId")
      return false
    }
    checkInHabitUseCase(progress)
    return true
  }

  companion object {
    private const val TAG = "HabitCheckInHandler"
  }
}
