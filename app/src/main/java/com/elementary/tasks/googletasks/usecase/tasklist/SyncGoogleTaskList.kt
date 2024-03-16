package com.elementary.tasks.googletasks.usecase.tasklist

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.googletasks.usecase.task.SyncGoogleTasks
import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTaskList
import com.elementary.tasks.googletasks.usecase.remote.DownloadGoogleTaskList

class SyncGoogleTaskList(
  private val gTasks: GTasks,
  private val downloadGoogleTaskList: DownloadGoogleTaskList,
  private val saveGoogleTaskList: SaveGoogleTaskList,
  private val syncGoogleTasks: SyncGoogleTasks
) {

  operator fun invoke(list: GoogleTaskList) {
    if (!gTasks.isLogged) {
      Traces.log("Sync task list - not logged")
      return
    }

    // Upload if not uploaded

    // Download remote version
    Traces.log("Sync task list - load remote version")
    val remote = downloadGoogleTaskList(list)

    // Save updated version to db
    Traces.log("Sync task list - save new version")
    saveGoogleTaskList(remote)

    // Sync Tasks
    Traces.log("Sync task list - sync tasks")
    syncGoogleTasks(remote)
  }
}
