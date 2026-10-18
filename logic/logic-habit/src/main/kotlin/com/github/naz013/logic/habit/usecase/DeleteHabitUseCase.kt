package com.github.naz013.logic.habit.usecase

import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.HabitRepository

class DeleteHabitUseCase(
  private val habitRepository: HabitRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {
  suspend operator fun invoke(id: String) {
    habitRepository.delete(id)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Habits,
      id = id
    )
    Logger.i(TAG, "Deleted habit with id = $id")
  }

  companion object {
    private const val TAG = "DeleteHabitUseCase"
  }
}
