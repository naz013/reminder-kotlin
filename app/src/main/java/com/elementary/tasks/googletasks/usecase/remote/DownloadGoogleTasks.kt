package com.elementary.tasks.googletasks.usecase.remote

import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList

class DownloadGoogleTasks(
  private val googleTasksApi: GoogleTasksApi
) {

  suspend operator fun invoke(googleTaskList: GoogleTaskList): List<GoogleTask> {
    return googleTasksApi.getTasks(googleTaskList.listId)
  }
}
