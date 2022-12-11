package com.elementary.tasks.core.view_models.google_tasks

import android.content.Context
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import java.io.IOException

abstract class BaseTaskListsViewModel(
  prefs: Prefs,
  protected val context: Context,
  protected val gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  protected val updatesHelper: UpdatesHelper,
  protected val googleTasksDao: GoogleTasksDao,
  protected val googleTaskListsDao: GoogleTaskListsDao
) : BaseDbViewModel(prefs, dispatcherProvider, workManagerProvider) {

  fun deleteGoogleTaskList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      val def = googleTaskList.def
      gTasks.deleteTaskList(googleTaskList.listId)
      googleTaskListsDao.delete(googleTaskList)
      googleTasksDao.deleteAll(googleTaskList.listId)
      if (def == 1) {
        val lists = googleTaskListsDao.all()
        if (lists.isNotEmpty()) {
          val taskList = lists[0]
          taskList.def = 1
          googleTaskListsDao.insert(taskList)
        }
      }
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun toggleTask(googleTask: GoogleTask) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      try {
        if (googleTask.status == GTasks.TASKS_NEED_ACTION) {
          gTasks.updateTaskStatus(GTasks.TASKS_COMPLETE, googleTask)
        } else {
          gTasks.updateTaskStatus(GTasks.TASKS_NEED_ACTION, googleTask)
        }
        postInProgress(false)
        postCommand(Commands.UPDATED)
        withUIContext {
          updatesHelper.updateTasksWidget()
        }
      } catch (e: IOException) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }
}
