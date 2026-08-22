package com.github.naz013.logic.routine.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RoutineExecutionRepository

/** Records one completed (or partially completed/skipped) focus-runner session. */
class RecordRoutineExecutionUseCase(
  private val routineExecutionRepository: RoutineExecutionRepository,
  private val nowDateTimeProvider: NowDateTimeProvider,
) {
  suspend operator fun invoke(
    routineId: String,
    completedStepIds: List<String>,
    totalTimeSpentSeconds: Int,
    totalStepsCount: Int
  ): RoutineExecutionRecord {
    val record = RoutineExecutionRecord(
      routineId = routineId,
      executedAt = nowDateTimeProvider.nowDateTime(),
      totalTimeSpentSeconds = totalTimeSpentSeconds,
      completedStepIds = completedStepIds,
      totalStepsCount = totalStepsCount
    )
    routineExecutionRepository.save(record)
    Logger.i(
      TAG,
      "Recorded routine execution: routineId=$routineId, " +
        "completedSteps=${completedStepIds.size}/$totalStepsCount"
    )
    return record
  }

  companion object {
    private const val TAG = "RecordRoutineExecutionUseCase"
  }
}
