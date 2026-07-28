package com.elementary.tasks.settings.export.work

import com.github.naz013.files.DataType
import com.github.naz013.sync.SyncApi
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class BackupSettingsTask(
  private val syncApi: SyncApi,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    syncApi.upload(DataType.Settings)
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "backup_settings"
  }
}
