package com.elementary.tasks.groups.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.sync.DataType

class DeleteReminderGroupUseCase(
  private val reminderGroupRepository: ReminderGroupRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  suspend operator fun invoke(groupId: String) {
    reminderGroupRepository.delete(groupId)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Groups,
      id = groupId
    )
    Logger.i(TAG, "Deleted reminder group with id = $groupId")
  }

  companion object {
    private const val TAG = "DeleteReminderGroupUseCase"
  }
}
