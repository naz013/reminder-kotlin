package com.elementary.tasks.core.cloud.worker

import com.github.naz013.files.DataType
import com.github.naz013.sync.SyncApi
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class DeleteTask(
  private val syncApi: SyncApi,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    val dataType =
      input
        .getString(WorkerData.DATA_TYPE)
        ?.takeIf { it.isNotEmpty() }
        ?.let { DataType.valueOf(it) }
    val itemId =
      input
        .getString(WorkerData.ITEM_ID)
        ?.takeIf { it.isNotEmpty() }
    val itemIds =
      input
        .getStringArray(WorkerData.ITEM_IDS)
        ?.toList()
        ?.takeIf { it.isNotEmpty() }
    if (dataType != null) {
      if (itemId != null) {
        syncApi.delete(dataType, itemId)
      } else if (itemIds != null) {
        syncApi.delete(dataType, itemIds)
      } else {
        syncApi.delete(dataType)
      }
    } else {
      syncApi.delete()
    }
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "cloud_delete"
  }
}
