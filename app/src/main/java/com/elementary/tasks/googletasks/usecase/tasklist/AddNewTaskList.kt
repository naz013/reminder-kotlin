package com.elementary.tasks.googletasks.usecase.tasklist

import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTaskList
import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTasks
import com.elementary.tasks.googletasks.usecase.remote.DownloadGoogleTasks
import com.github.naz013.domain.GoogleTaskList

class AddNewTaskList(
  private val saveGoogleTaskList: SaveGoogleTaskList,
  private val downloadGoogleTasks: DownloadGoogleTasks,
  private val saveGoogleTasks: SaveGoogleTasks
) {

  suspend operator fun invoke(googleTaskList: GoogleTaskList) {
    // Save to DB
    saveGoogleTaskList(googleTaskList)

    // Download Tasks for Task List
    val tasks = downloadGoogleTasks(googleTaskList)
    if (tasks.isNotEmpty()) {
      saveGoogleTasks(tasks)
    }
  }
}
