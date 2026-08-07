package com.github.naz013.feature.googletask.usecase.tasklist

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.googletask.usecase.db.SaveGoogleTaskList
import com.github.naz013.feature.googletask.usecase.db.SaveGoogleTasks
import com.github.naz013.feature.googletask.usecase.remote.DownloadGoogleTasks

class AddNewTaskList(
  private val saveGoogleTaskList: SaveGoogleTaskList,
  private val downloadGoogleTasks: DownloadGoogleTasks,
  private val saveGoogleTasks: SaveGoogleTasks,
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
