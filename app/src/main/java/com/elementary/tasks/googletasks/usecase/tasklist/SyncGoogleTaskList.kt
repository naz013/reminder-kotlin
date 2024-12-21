package com.elementary.tasks.googletasks.usecase.tasklist

import com.elementary.tasks.core.cloud.GTasks
import com.github.naz013.domain.GoogleTaskList
import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTaskList
import com.elementary.tasks.googletasks.usecase.remote.DownloadGoogleTaskList
import com.elementary.tasks.googletasks.usecase.task.SyncGoogleTasks
import com.github.naz013.logging.Logger

class SyncGoogleTaskList(
  private val gTasks: GTasks,
  private val downloadGoogleTaskList: DownloadGoogleTaskList,
  private val saveGoogleTaskList: SaveGoogleTaskList,
  private val syncGoogleTasks: SyncGoogleTasks
) {

  suspend operator fun invoke(list: GoogleTaskList) {
    if (!gTasks.isLogged) {
      Logger.i("Sync task list - not logged")
      return
    }

    // Upload if not uploaded

    // Download remote version
    Logger.i("Sync task list - load remote version")
    val remote = downloadGoogleTaskList(list)

    // Save updated version to db
    Logger.i("Sync task list - save new version")
    saveGoogleTaskList(remote)

    // Sync Tasks
    Logger.i("Sync task list - sync tasks")
    syncGoogleTasks(remote)
  }
}
