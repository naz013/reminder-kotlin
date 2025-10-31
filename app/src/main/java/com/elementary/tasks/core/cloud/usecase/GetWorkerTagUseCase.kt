package com.elementary.tasks.core.cloud.usecase

import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.sync.DataType

class GetWorkerTagUseCase {

  operator fun invoke(
    workType: WorkType,
    dataType: DataType? = null,
    id: String? = null
  ): String {
    val builder = StringBuilder()
    builder.append(when (workType) {
      WorkType.Upload -> "UPLOAD"
      WorkType.Delete -> "DELETE"
      WorkType.Sync -> "SYNC"
      WorkType.ForceUpload -> "FORCE_UPLOAD"
      WorkType.ForceSync -> "SYNC"
    })
    dataType?.also { builder.append("_").append(it.name) }
    id?.also { builder.append("_").append(it) }
    return builder.toString()
  }
}
