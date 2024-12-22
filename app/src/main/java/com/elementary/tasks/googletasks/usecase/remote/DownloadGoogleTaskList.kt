package com.elementary.tasks.googletasks.usecase.remote

import com.elementary.tasks.googletasks.usecase.GoogleTaskListFactory
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTaskList

class DownloadGoogleTaskList(
  private val googleTasksApi: GoogleTasksApi,
  private val googleTaskListFactory: GoogleTaskListFactory
) {

  suspend operator fun invoke(taskList: GoogleTaskList): GoogleTaskList {
    return googleTasksApi.getTaskList(taskList.listId)?.let {
      googleTaskListFactory.update(taskList, it)
    } ?: taskList
  }
}
