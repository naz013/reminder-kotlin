package com.elementary.tasks.core.view_models.google_tasks

import android.content.Context
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import java.io.IOException

abstract class BaseTaskListsViewModel(
  appDb: AppDb,
  prefs: Prefs,
  protected val context: Context,
  protected val gTasks: GTasks
) : BaseDbViewModel(appDb, prefs) {

  fun deleteGoogleTaskList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      val def = googleTaskList.def
      gTasks.deleteTaskList(googleTaskList.listId)
      appDb.googleTaskListsDao().delete(googleTaskList)
      appDb.googleTasksDao().deleteAll(googleTaskList.listId)
      if (def == 1) {
        val lists = appDb.googleTaskListsDao().all()
        if (lists.isNotEmpty()) {
          val taskList = lists[0]
          taskList.def = 1
          appDb.googleTaskListsDao().insert(taskList)
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
          UpdatesHelper.updateTasksWidget(context)
        }
      } catch (e: IOException) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }
}
