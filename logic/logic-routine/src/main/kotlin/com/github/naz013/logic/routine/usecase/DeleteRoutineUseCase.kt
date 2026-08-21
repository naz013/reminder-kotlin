package com.github.naz013.logic.routine.usecase

import com.github.naz013.domain.TaggedItemType
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.RoutineExecutionRepository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository

class DeleteRoutineUseCase(
  private val routineRepository: RoutineRepository,
  private val routineExecutionRepository: RoutineExecutionRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  // Doesn't yet clear a linked ReminderV2 trigger (Routine.reminderId) - deferred until
  // RoutineScheduleBridge exists to own that scheduling relationship.
  suspend operator fun invoke(id: String) {
    routineRepository.delete(id)
    routineExecutionRepository.deleteByRoutineId(id)
    tagAssignmentRepository.detachAll(id, TaggedItemType.ROUTINE)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Routines,
      id = id
    )
    Logger.i(TAG, "Deleted routine with id = $id")
  }

  companion object {
    private const val TAG = "DeleteRoutineUseCase"
  }
}
