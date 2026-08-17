package com.github.naz013.logic.googletask.usecase.tasklist

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.logic.googletask.usecase.remote.DownloadGoogleTasks
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository

internal class AddNewTaskList(
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val downloadGoogleTasks: DownloadGoogleTasks,
  private val googleTaskRepository: GoogleTaskRepository,
) {
  suspend operator fun invoke(googleTaskList: GoogleTaskList) {
    // Save to DB
    googleTaskListRepository.save(googleTaskList)

    // Download Tasks for Task List
    val tasks = downloadGoogleTasks(googleTaskList)
    if (tasks.isNotEmpty()) {
      googleTaskRepository.saveAll(tasks)
    }
  }
}
