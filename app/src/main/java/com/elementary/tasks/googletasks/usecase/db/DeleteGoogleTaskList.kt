package com.elementary.tasks.googletasks.usecase.db

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.models.GoogleTaskList

class DeleteGoogleTaskList(
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val deleteGoogleTasks: DeleteGoogleTasks,
  private val getGoogleTasksByList: GetGoogleTasksByList
) {

  operator fun invoke(googleTaskList: GoogleTaskList) {
    Traces.log("Delete Google task list")
    googleTaskListsDao.delete(googleTaskList)
    val googleTasks = getGoogleTasksByList(googleTaskList)
    deleteGoogleTasks(googleTasks)
  }
}
