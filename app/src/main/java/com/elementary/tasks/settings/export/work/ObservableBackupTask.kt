package com.elementary.tasks.settings.export.work

import com.github.naz013.logging.Logger
import com.github.naz013.sync.SyncApi
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

/**
 * Performs an observable backup, reporting progress under [KEY_IS_IN_PROGRESS] so UI
 * can reflect the running state via [com.github.naz013.workapi.WorkScheduler.observeUniqueWork].
 */
class ObservableBackupTask(
  private val syncApi: SyncApi,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult =
    try {
      Logger.i(TASK_KEY, "Starting observable backup")
      progress.report(progressData(true))

      syncApi.upload()

      Logger.i(TASK_KEY, "Observable backup completed successfully")
      progress.report(progressData(false))
      TaskResult.Success
    } catch (e: Exception) {
      Logger.e(TASK_KEY, "Observable backup failed", e)
      progress.report(progressData(false))
      TaskResult.Failure
    }

  private fun progressData(isInProgress: Boolean): TaskData = TaskData.builder().putBoolean(KEY_IS_IN_PROGRESS, isInProgress).build()

  companion object {
    const val TASK_KEY = "observable_backup"
    const val KEY_IS_IN_PROGRESS = "is_in_progress"
  }
}
