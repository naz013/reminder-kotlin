package com.elementary.tasks.googletasks.usecase.task

import com.elementary.tasks.googletasks.usecase.db.DeleteGoogleTasks
import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTasks
import com.elementary.tasks.googletasks.usecase.remote.DownloadGoogleTasks
import com.elementary.tasks.googletasks.usecase.remote.UploadGoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskRepository

class SyncGoogleTasks(
  private val googleTaskRepository: GoogleTaskRepository,
  private val uploadGoogleTask: UploadGoogleTask,
  private val downloadGoogleTasks: DownloadGoogleTasks,
  private val saveGoogleTasks: SaveGoogleTasks,
  private val deleteGoogleTasks: DeleteGoogleTasks
) {

  suspend operator fun invoke(taskList: GoogleTaskList) {
    // Get local tasks
    val local = googleTaskRepository.getAllByList(taskList.listId)
    Logger.i("Sync tasks for list - number of local tasks = ${local.size}")

    // Upload changed tasks
    Logger.i("Sync tasks for list - upload")
    local.filterNot { it.uploaded }.forEach { uploadGoogleTask(it) }

    // Download remote tasks
    val remote = downloadGoogleTasks(taskList)
    Logger.i("Sync tasks for list - remote tasks = ${remote.size}")
    Logger.d("Remote tasks = $remote")

    // Save new tasks
    Logger.i("Sync tasks for list - save remote version")
    saveGoogleTasks(remote)

    val remoteMap = remote.associateBy { it.taskId }
    val localDelete = local.filterNot { remoteMap.containsKey(it.taskId) }
    Logger.i("Sync tasks for list - delete local versions = ${localDelete.size}")
    deleteGoogleTasks(localDelete)
  }
}
