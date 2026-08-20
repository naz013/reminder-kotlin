package com.github.naz013.logic.schedule

import com.github.naz013.files.DataType

interface ScheduleBackgroundWorkUseCase {
  @IgnorableReturnValue
  operator fun invoke(
    workType: WorkType,
    dataType: DataType? = null,
    id: String? = null,
    ids: List<String>? = null,
  ): String?
}
