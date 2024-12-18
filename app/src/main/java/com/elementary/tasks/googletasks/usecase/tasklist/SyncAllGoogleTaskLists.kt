package com.elementary.tasks.googletasks.usecase.tasklist

import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.googletasks.usecase.db.DeleteGoogleTaskList
import com.github.naz013.logging.Logger

class SyncAllGoogleTaskLists(
  private val gTasks: GTasks,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val syncGoogleTaskList: SyncGoogleTaskList,
  private val addNewTaskList: AddNewTaskList,
  private val deleteGoogleTaskList: DeleteGoogleTaskList
) {

  operator fun invoke() {
    if (!gTasks.isLogged) {
      Logger.i("Sync all gtasks - not logged")
      return
    }

    // Get all Google Task Lists from DB
    val localTaskLists = googleTaskListsDao.all()
    Logger.i("Sync all gtasks, number of local = ${localTaskLists.size}")

    // Sync each of them
    localTaskLists.forEach { syncGoogleTaskList(it) }

    // Download latest version of task lists
    val remoteTaskLists = gTasks.getTaskLists()
    Logger.i("Sync all gtasks, number of remote = ${remoteTaskLists.size}")

    // Save updated to DB
    val localMap = localTaskLists.associateBy { it.listId }
    remoteTaskLists.filterNot { localMap.containsKey(it.id) }
      .also {
        if (it.isNotEmpty()) {
          Logger.i("Sync all gtasks, add new task lists = ${it.size}")
        } else {
          Logger.i("Sync all gtasks, no new task lists")
        }
      }
      .forEach { addNewTaskList(it) }

    val remoteMap = remoteTaskLists.associateBy { it.id }
    localTaskLists.filterNot { remoteMap.containsKey(it.listId) }.forEach {
      deleteGoogleTaskList(it)
    }

    // Set default Task list if not present
    if (googleTaskListsDao.defaultGoogleTaskList() == null) {
      Logger.i("Sync all gtasks, set default task list")
      googleTaskListsDao.all().firstOrNull()
        ?.apply { def = 1 }
        ?.also { googleTaskListsDao.insert(it) }
    }
  }
}
