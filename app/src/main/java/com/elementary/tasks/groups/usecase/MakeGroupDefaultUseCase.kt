package com.elementary.tasks.groups.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.sync.DataType

class MakeGroupDefaultUseCase(
  private val reminderGroupRepository: ReminderGroupRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {
  suspend operator fun invoke(id: String) {
    reminderGroupRepository.getById(id) ?: run {
      Logger.e(TAG, "Group not found: $id")
      return
    }
    reminderGroupRepository
      .getAll()
      .filter { it.isDefaultGroup }
      .filterNot { it.groupUuId == id }
      .forEach {
        reminderGroupRepository.setDefaultGroup(it.groupUuId, false)
        reminderGroupRepository.updateSyncState(it.groupUuId, SyncState.WaitingForUpload)
      }

    reminderGroupRepository.setDefaultGroup(id, true)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Groups,
      id = null,
    )
    Logger.i(TAG, "Group set as default: $id")
  }

  companion object {
    private const val TAG = "MakeGroupDefaultUseCase"
  }
}
