package com.github.naz013.feature.googletask.usecase.task

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.feature.googletask.usecase.remote.DownloadGoogleTasks
import com.github.naz013.feature.googletask.usecase.remote.UploadGoogleTask
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.TagAssignmentRepository

internal class SyncGoogleTasks(
  private val googleTaskRepository: GoogleTaskRepository,
  private val uploadGoogleTask: UploadGoogleTask,
  private val downloadGoogleTasks: DownloadGoogleTasks,
  private val tagAssignmentRepository: TagAssignmentRepository,
) {
  suspend operator fun invoke(taskList: GoogleTaskList) {
    // Get local tasks
    val local = googleTaskRepository.getAllByList(taskList.listId)
    Logger.i(TAG, "Sync tasks for list - number of local tasks = ${local.size}")

    // Upload changed tasks
    Logger.i(TAG, "Sync tasks for list - upload")
    local.filterNot { it.uploaded }.forEach { uploadGoogleTask(it) }

    // Download remote tasks
    val remote = downloadGoogleTasks(taskList)
    Logger.i(TAG, "Sync tasks for list - remote tasks = ${remote.size}")
    Logger.d(TAG, "Remote tasks = $remote")

    // Save new tasks
    Logger.i(TAG, "Sync tasks for list - save remote version")
    googleTaskRepository.saveAll(remote)

    val remoteMap = remote.associateBy { it.taskId }
    val localDelete = local.filterNot { remoteMap.containsKey(it.taskId) }
    Logger.i(TAG, "Sync tasks for list - delete local versions = ${localDelete.size}")

    localDelete.map { it.taskId }
      .takeIf { it.isNotEmpty() }
      ?.let { googleTaskRepository.deleteAll(it) }
    localDelete.forEach { tagAssignmentRepository.detachAll(it.taskId, TaggedItemType.GOOGLE_TASK) }
  }

  companion object {
    private const val TAG = "SyncGoogleTasks"
  }
}
