package com.elementary.tasks.googletasks.usecase.remote

import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.googletasks.usecase.GoogleTaskFactory

class DownloadGoogleTasks(
  private val gTasks: GTasks,
  private val googleTaskFactory: GoogleTaskFactory
) {

  operator fun invoke(googleTaskList: GoogleTaskList): List<GoogleTask> {
    if (!gTasks.isLogged) {
      return emptyList()
    }
    return gTasks.getTasks(googleTaskList.listId).map {
      googleTaskFactory.create(it, googleTaskList.listId)
    }
  }
}
