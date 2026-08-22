package com.github.naz013.feature.routine

import com.github.naz013.logging.Logger
import com.github.naz013.logic.routine.usecase.RoutineRecurrenceResetUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

/** Periodic (~daily) background check for every recurring routine's cycle, so a routine the user
 * never opens that day still gets its day-boundary reset - and, via
 * [RoutineRecurrenceResetUseCase], its [com.github.naz013.domain.routine.RoutineExecutionRecord]
 * history entry - instead of relying solely on the lazy on-view check in
 * `RoutinesListViewModel`/`RoutinePreviewViewModel`. Both call sites converge on the same use case,
 * which is idempotent per calendar day, so running this alongside the lazy check never double-records. */
class RoutineRecurrenceResetTask(
  private val routineRepository: RoutineRepository,
  private val routineRecurrenceResetUseCase: RoutineRecurrenceResetUseCase,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    val recurringRoutines = routineRepository.getAll().filter { it.recurrence != null }
    recurringRoutines.forEach { routineRecurrenceResetUseCase(it) }
    Logger.i(TASK_KEY, "Checked ${recurringRoutines.size} recurring routine(s) for a new cycle.")
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "routine_recurrence_reset"
  }
}
