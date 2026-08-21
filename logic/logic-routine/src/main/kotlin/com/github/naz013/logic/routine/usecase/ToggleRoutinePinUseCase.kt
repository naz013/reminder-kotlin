package com.github.naz013.logic.routine.usecase

import com.github.naz013.domain.routine.Routine
import com.github.naz013.logging.Logger

/** Toggles whether a routine is pinned to the top of the routines list. */
class ToggleRoutinePinUseCase(
  private val saveRoutineUseCase: SaveRoutineUseCase,
) {
  suspend operator fun invoke(routine: Routine): Routine {
    val updated = routine.copy(isPinned = !routine.isPinned)
    Logger.i(TAG, "Toggling pinned state for id=${routine.id}, isPinned=${updated.isPinned}")
    return saveRoutineUseCase(updated)
  }

  companion object {
    private const val TAG = "ToggleRoutinePinUseCase"
  }
}
