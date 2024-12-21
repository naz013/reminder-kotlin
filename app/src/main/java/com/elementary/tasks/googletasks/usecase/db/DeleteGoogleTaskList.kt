package com.elementary.tasks.googletasks.usecase.db

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository

class DeleteGoogleTaskList(
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val deleteGoogleTasks: DeleteGoogleTasks,
  private val getGoogleTasksByList: GetGoogleTasksByList
) {

  suspend operator fun invoke(googleTaskList: GoogleTaskList) {
    Logger.i("Delete Google task list")
    googleTaskListRepository.delete(googleTaskList.listId)
    val googleTasks = getGoogleTasksByList(googleTaskList)
    deleteGoogleTasks(googleTasks)
  }
}
