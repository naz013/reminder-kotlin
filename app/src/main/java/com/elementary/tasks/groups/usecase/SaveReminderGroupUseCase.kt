package com.elementary.tasks.groups.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.sync.DataType

class SaveReminderGroupUseCase(
  private val reminderGroupRepository: ReminderGroupRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  suspend operator fun invoke(reminderGroup: ReminderGroup) {
    // TODO: consider the version increment
    reminderGroupRepository.save(reminderGroup)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Groups,
      id = reminderGroup.groupUuId
    )
    Logger.i(TAG, "Saved reminder group: ${reminderGroup.groupUuId}")
  }

  companion object {
    private const val TAG = "SaveReminderGroupUseCase"
  }
}
