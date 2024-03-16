package com.elementary.tasks.googletasks.usecase.db

import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTask

class DeleteGoogleTasks(
  private val googleTasksDao: GoogleTasksDao
) {

  operator fun invoke(tasks: List<GoogleTask>) {
    if (tasks.isEmpty()) {
      return
    }
    googleTasksDao.deleteAll(tasks)
  }
}
