package com.github.naz013.logic.routine.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.routine.Routine
import com.github.naz013.logging.Logger

/** Auto-resets a recurring routine's steps once a new calendar day starts, so a daily/weekly
 * routine doesn't stay stuck showing yesterday's completed checkmarks. On-demand routines
 * ([Routine.recurrence] == null) are left untouched - only a scheduled routine auto-resets.
 *
 * Resets once per calendar day regardless of the recurrence rule's actual cadence (e.g. a
 * Mon/Wed/Fri routine still resets every day it's opened, not just on its scheduled days) -
 * evaluating the real RRULE here would require pulling in the icalendar module, which isn't
 * justified for "did the user already see today's fresh checklist". */
class RoutineRecurrenceResetUseCase(
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val saveRoutineUseCase: SaveRoutineUseCase,
) {
  suspend operator fun invoke(routine: Routine): Routine {
    if (routine.recurrence == null) {
      return routine
    }
    val today = nowDateTimeProvider.nowDate()
    val lastResetDate = routine.lastResetAt?.toLocalDate()
    if (lastResetDate != null && !lastResetDate.isBefore(today)) {
      return routine
    }
    Logger.i(TAG, "Resetting routine steps for new cycle: id=${routine.id}")
    val reset = routine.copy(
      steps = routine.steps.map { it.copy(isCompleted = false) },
      lastResetAt = nowDateTimeProvider.nowDateTime()
    )
    return saveRoutineUseCase(reset)
  }

  companion object {
    private const val TAG = "RoutineRecurrenceResetUseCase"
  }
}
