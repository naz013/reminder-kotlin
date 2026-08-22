package com.github.naz013.logic.routine.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.RoutineRepository

class SaveRoutineUseCase(
  private val routineRepository: RoutineRepository,
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  suspend operator fun invoke(routine: Routine): Routine {
    val toSave = routine.copy(
      updatedAt = nowDateTimeProvider.nowDateTime(),
      sync = routine.sync.copy(
        version = routine.sync.version + 1,
        syncState = SyncState.WaitingForUpload
      )
    )
    routineRepository.save(toSave)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Routines,
      id = toSave.id
    )
    Logger.i(TAG, "Saved routine: ${toSave.id}")
    return toSave
  }

  companion object {
    private const val TAG = "SaveRoutineUseCase"
  }
}
