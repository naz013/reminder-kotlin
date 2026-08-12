package com.github.naz013.feature.googletask.usecase.db

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.TagAssignmentRepository

internal class DeleteGoogleTaskList(
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
) {
  suspend operator fun invoke(googleTaskList: GoogleTaskList) {
    Logger.i(TAG, "Delete Google task list")
    googleTaskListRepository.delete(googleTaskList.listId)
    val googleTasks = googleTaskRepository.getAllByList(googleTaskList.listId)
    googleTasks.map { it.taskId }.takeIf { it.isNotEmpty() }?.let {
      googleTaskRepository.deleteAll(it)
    }
    googleTasks.forEach { tagAssignmentRepository.detachAll(it.taskId, TaggedItemType.GOOGLE_TASK) }
  }

  companion object {
    private const val TAG = "DeleteGoogleTaskList"
  }
}
