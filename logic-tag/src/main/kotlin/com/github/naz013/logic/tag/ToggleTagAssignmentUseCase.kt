package com.github.naz013.logic.tag

import com.github.naz013.domain.TaggedItemType
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.TagAssignmentRepository

class ToggleTagAssignmentUseCase(
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
) {

  suspend operator fun invoke(
    id: String,
    taggedItemType: TaggedItemType,
    tagId: String,
    isSelected: Boolean
  ) {
    if (isSelected) {
      tagAssignmentRepository.detach(id, taggedItemType, tagId)
    } else {
      tagAssignmentRepository.attach(id, taggedItemType, tagId)
    }
    scheduleBackgroundWorkUseCase(workType = WorkType.Upload, dataType = DataType.TagAssignments)
    Logger.v(TAG, "Toggled ($isSelected) the tag ($tagId) assignment for target: id=$id, type=$taggedItemType")
  }

  companion object {
    private const val TAG = "ToggleTagAssignmentUseCase"
  }
}
