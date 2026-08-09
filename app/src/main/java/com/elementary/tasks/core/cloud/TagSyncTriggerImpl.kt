package com.elementary.tasks.core.cloud

import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.files.DataType
import com.github.naz013.repository.TagSyncTrigger

class TagSyncTriggerImpl(
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) : TagSyncTrigger {
  override fun onTagSaved(id: String) {
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Tags,
      id = id,
    )
  }

  override fun onTagDeleted(id: String) {
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.Tags,
      id = id,
    )
  }
}
