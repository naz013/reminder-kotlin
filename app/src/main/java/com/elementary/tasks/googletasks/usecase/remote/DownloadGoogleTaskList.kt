package com.elementary.tasks.googletasks.usecase.remote

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.googletasks.usecase.GoogleTaskListFactory

class DownloadGoogleTaskList(
  private val gTasks: GTasks,
  private val googleTaskListFactory: GoogleTaskListFactory
) {

  operator fun invoke(taskList: GoogleTaskList): GoogleTaskList {
    if (!gTasks.isLogged) {
      Traces.log("Download task list - not logged, return local")
      return taskList
    }

    return gTasks.getTaskList(taskList.listId)?.let {
      googleTaskListFactory.update(taskList, it)
    } ?: taskList
  }
}
