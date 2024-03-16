package com.elementary.tasks.googletasks.usecase.db

import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList

class GetGoogleTasksByList(
  private val googleTasksDao: GoogleTasksDao
) {

  operator fun invoke(list: GoogleTaskList): List<GoogleTask> {
    return googleTasksDao.getAllByList(list.listId)
  }
}
