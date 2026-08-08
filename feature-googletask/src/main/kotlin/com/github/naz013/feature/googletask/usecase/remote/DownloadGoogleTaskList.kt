package com.github.naz013.feature.googletask.usecase.remote

import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.googletask.usecase.GoogleTaskListFactory

internal class DownloadGoogleTaskList(
  private val googleTasksApi: GoogleTasksApi,
  private val googleTaskListFactory: GoogleTaskListFactory,
) {
  suspend operator fun invoke(taskList: GoogleTaskList): GoogleTaskList =
    googleTasksApi.getTaskList(taskList.listId)?.let {
      googleTaskListFactory.update(taskList, it)
    } ?: taskList
}
