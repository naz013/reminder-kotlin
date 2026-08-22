package com.github.naz013.logic.routine.usecase

import com.github.naz013.domain.routine.Routine
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RoutineRepository

/** Manually unchecks every step of a routine on demand (the preview screen's "Reset Steps" action),
 * as opposed to [RoutineRecurrenceResetUseCase]'s automatic reset on a new recurrence cycle. */
class ResetRoutineStepsUseCase(
  private val routineRepository: RoutineRepository,
  private val saveRoutineUseCase: SaveRoutineUseCase,
) {
  suspend operator fun invoke(routineId: String): Routine? {
    val routine = routineRepository.getById(routineId) ?: run {
      Logger.e(TAG, "Routine not found: $routineId")
      return null
    }
    if (routine.steps.none { it.isCompleted }) {
      return routine
    }
    val updated = routine.copy(steps = routine.steps.map { it.copy(isCompleted = false) })
    return saveRoutineUseCase(updated)
  }

  companion object {
    private const val TAG = "ResetRoutineStepsUseCase"
  }
}
