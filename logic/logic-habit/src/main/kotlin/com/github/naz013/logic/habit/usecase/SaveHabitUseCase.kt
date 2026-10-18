package com.github.naz013.logic.habit.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.HabitRepository

class SaveHabitUseCase(
  private val habitRepository: HabitRepository,
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {
  suspend operator fun invoke(habit: Habit): Habit {
    val toSave = habit.copy(
      updatedAt = nowDateTimeProvider.nowDateTime(),
      sync = habit.sync.copy(
        version = habit.sync.version + 1,
        syncState = SyncState.WaitingForUpload
      )
    )
    habitRepository.save(toSave)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Habits,
      id = toSave.id
    )
    Logger.i(TAG, "Saved habit: ${toSave.id}")
    return toSave
  }

  companion object {
    private const val TAG = "SaveHabitUseCase"
  }
}
