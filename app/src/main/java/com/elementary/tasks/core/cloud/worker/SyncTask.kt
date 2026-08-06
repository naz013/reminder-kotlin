package com.elementary.tasks.core.cloud.worker

import com.github.naz013.files.DataType
import com.github.naz013.sync.SyncApi
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class SyncTask(
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
    val force = input.getBoolean(WorkerData.FORCE, false)
    if (dataType != null) {
      if (itemId != null) {
        syncApi.sync(dataType, itemId, force)
      } else {
        syncApi.sync(dataType, force)
      }
    } else {
      syncApi.sync(force)
    }
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "cloud_sync"
  }
}
