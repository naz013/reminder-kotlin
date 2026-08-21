package com.github.naz013.logic.routine.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.routine.Routine
import com.github.naz013.logging.Logger

/** Auto-resets a recurring routine's steps once a new calendar day starts, so a daily/weekly
 * routine doesn't stay stuck showing yesterday's completed checkmarks. On-demand routines
 * ([Routine.recurrence] == null) are left untouched entirely - they're never auto-reset, only
 * ever reset manually by the user (see `ResetRoutineStepsUseCase`).
 *
 * Resets once per calendar day regardless of the recurrence rule's actual cadence (e.g. a
 * Mon/Wed/Fri routine still resets every day it's opened, not just on its scheduled days) -
 * evaluating the real RRULE here would require pulling in the icalendar module, which isn't
 * justified for "did the user already see today's fresh checklist". Concretely: whatever steps
 * were completed up to the moment the calendar day rolls over count as that cycle's result - the
 * user can check/uncheck freely all day, right up until midnight.
 *
 * Before wiping the steps, the cycle's result (however many steps ended up completed, even zero)
 * is written to [RoutineExecutionRepository] via [RecordRoutineExecutionUseCase] so habit-tracking
 * history isn't lost on reset. This runs lazily on next view (list/preview screen load), not on a
 * background schedule - if the app isn't opened for several days, only one summary is written (for
 * whatever was completed as of that later view), not one per skipped day in between.
 *
 * [Routine.lastResetAt] doubles as "when did the current cycle start" - `SaveRoutineUseCase`'s
 * callers are responsible for setting it to the save time when recurrence is newly turned on, so
 * the very first cycle starts at save time rather than immediately triggering a same-day reset. */
class RoutineRecurrenceResetUseCase(
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val saveRoutineUseCase: SaveRoutineUseCase,
  private val recordRoutineExecutionUseCase: RecordRoutineExecutionUseCase,
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
    recordRoutineExecutionUseCase(
      routineId = routine.id,
      completedStepIds = routine.steps.filter { it.isCompleted }.map { it.id },
      totalTimeSpentSeconds = 0,
      totalStepsCount = routine.steps.size,
    )
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
