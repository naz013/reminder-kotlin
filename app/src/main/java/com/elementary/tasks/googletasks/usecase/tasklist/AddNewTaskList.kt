package com.elementary.tasks.googletasks.usecase.tasklist

import com.elementary.tasks.googletasks.usecase.GetRandomGoogleTaskListColor
import com.elementary.tasks.googletasks.usecase.GoogleTaskListFactory
import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTaskList
import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTasks
import com.elementary.tasks.googletasks.usecase.remote.DownloadGoogleTasks
import com.google.api.services.tasks.model.TaskList

class AddNewTaskList(
  private val saveGoogleTaskList: SaveGoogleTaskList,
  private val googleTaskListFactory: GoogleTaskListFactory,
  private val getRandomGoogleTaskListColor: GetRandomGoogleTaskListColor,
  private val downloadGoogleTasks: DownloadGoogleTasks,
  private val saveGoogleTasks: SaveGoogleTasks
) {

  operator fun invoke(taskList: TaskList) {
    // Create DB object
    val googleTaskList = googleTaskListFactory.create(taskList, getRandomGoogleTaskListColor())

    // Save to DB
    saveGoogleTaskList(googleTaskList)

    // Download Tasks for Task List
    val tasks = downloadGoogleTasks(googleTaskList)
    if (tasks.isNotEmpty()) {
      saveGoogleTasks(tasks)
    }
  }
}
