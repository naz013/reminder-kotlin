package com.github.naz013.feature.googletask.usecase.tasklist

import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.googletask.usecase.remote.DownloadGoogleTaskList
import com.github.naz013.feature.googletask.usecase.task.SyncGoogleTasks
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository

internal class SyncGoogleTaskList(
  private val downloadGoogleTaskList: DownloadGoogleTaskList,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val syncGoogleTasks: SyncGoogleTasks,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
) {
  suspend operator fun invoke(list: GoogleTaskList) {
    if (!googleTasksAuthManager.isAuthorized()) {
      Logger.w(TAG, "Sync task list - not logged")
      return
    }

    // Upload if not uploaded

    // Download remote version
    Logger.i(TAG, "Sync task list - load remote version")
    val remote = downloadGoogleTaskList(list)

    // Save updated version to db
    Logger.i(TAG, "Sync task list - save new version")
    googleTaskListRepository.save(remote)

    // Sync Tasks
    Logger.i(TAG, "Sync task list - sync tasks")
    syncGoogleTasks(remote)
  }

  companion object {
    private const val TAG = "SyncGoogleTaskList"
  }
}
