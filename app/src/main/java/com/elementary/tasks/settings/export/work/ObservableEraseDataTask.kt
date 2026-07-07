package com.elementary.tasks.settings.export.work

import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.logging.Logger
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

/**
 * Erases all cloud data, reporting progress under [KEY_IS_IN_PROGRESS] so UI can reflect
 * the running state via [com.github.naz013.workapi.WorkScheduler.observeUniqueWork].
 */
class ObservableEraseDataTask(
  private val googleDriveApi: GoogleDriveApi,
  private val dropboxApi: DropboxApi,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult =
    try {
      Logger.i(TASK_KEY, "Starting observable erase data work")
      progress.report(progressData(true))

      googleDriveApi.removeAllData()
      dropboxApi.removeAllData()

      Logger.i(TASK_KEY, "Observable erase data work completed successfully")
      progress.report(progressData(false))
      TaskResult.Success
    } catch (e: Exception) {
      Logger.e(TASK_KEY, "Observable erase data work failed", e)
      progress.report(progressData(false))
      TaskResult.Failure
    }

  private fun progressData(isInProgress: Boolean): TaskData = TaskData.builder().putBoolean(KEY_IS_IN_PROGRESS, isInProgress).build()

  companion object {
    const val TASK_KEY = "observable_erase_data"
    const val KEY_IS_IN_PROGRESS = "is_in_progress"
  }
}
