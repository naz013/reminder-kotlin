package com.github.naz013.logic.habit.nudge

import com.github.naz013.logging.Logger
import com.github.naz013.repository.HabitRepository

class RescheduleHabitNudgesUseCase(
  private val habitRepository: HabitRepository,
  private val scheduleHabitNudgeUseCase: ScheduleHabitNudgeUseCase
) {
  suspend operator fun invoke() {
    val habits = habitRepository.getAll()
    habits.forEach { scheduleHabitNudgeUseCase(it) }
    Logger.i(TAG, "Rescheduled nudges for ${habits.size} habit(s)")
  }

  companion object {
    private const val TAG = "RescheduleHabitNudges"
  }
}
