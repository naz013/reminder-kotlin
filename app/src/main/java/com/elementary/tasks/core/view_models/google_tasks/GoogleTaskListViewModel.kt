package com.elementary.tasks.core.view_models.google_tasks

import android.content.Context
import androidx.lifecycle.LiveData
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.google.api.services.tasks.model.TaskLists
import timber.log.Timber
import java.io.IOException
import java.util.Random

class GoogleTaskListViewModel(
  listId: String,
  appDb: AppDb,
  prefs: Prefs,
  context: Context,
  gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  updatesHelper: UpdatesHelper
) : BaseTaskListsViewModel(
  appDb,
  prefs,
  context,
  gTasks,
  dispatcherProvider,
  workManagerProvider,
  updatesHelper
) {

  var googleTaskList: LiveData<GoogleTaskList>
  val defaultTaskList = appDb.googleTaskListsDao().loadDefault()
  var googleTasks: LiveData<List<GoogleTask>>
  private var isSyncing = false

  init {
    Timber.d("GoogleTaskListViewModel: $listId")
    googleTaskList = appDb.googleTaskListsDao().loadById(listId)
    googleTasks = appDb.googleTasksDao().loadAllByList(listId)
  }

  fun sync() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    if (isSyncing) return
    isSyncing = true
    postInProgress(true)
    launchDefault {
      var lists: TaskLists? = null
      try {
        lists = gTasks.taskLists()
      } catch (e: IOException) {
        e.printStackTrace()
      }

      if (lists != null && lists.size > 0 && lists.items != null) {
        for (item in lists.items) {
          val listId = item.id
          var taskList = appDb.googleTaskListsDao().getById(listId)
          if (taskList != null) {
            taskList.update(item)
          } else {
            val r = Random()
            val color = r.nextInt(15)
            taskList = GoogleTaskList(item, color)
          }
          appDb.googleTaskListsDao().insert(taskList)
          val tasks = gTasks.getTasks(listId)
          if (tasks.isEmpty()) {
            withUIContext {
              postInProgress(false)
              postCommand(Commands.UPDATED)
              updatesHelper.updateTasksWidget()
            }
          } else {
            val googleTasks = ArrayList<GoogleTask>()
            for (task in tasks) {
              var googleTask = appDb.googleTasksDao().getById(task.id)
              if (googleTask != null) {
                googleTask.listId = listId
                googleTask.update(task)
              } else {
                googleTask = GoogleTask(task, listId)
              }
              googleTasks.add(googleTask)
            }
            appDb.googleTasksDao().insertAll(googleTasks)
            withUIContext {
              postInProgress(false)
              postCommand(Commands.UPDATED)
              updatesHelper.updateTasksWidget()
            }
          }
        }
      }
      isSyncing = false
    }
  }

  fun newGoogleTaskList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      gTasks.insertTasksList(googleTaskList.title, googleTaskList.color)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun updateGoogleTaskList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      appDb.googleTaskListsDao().insert(googleTaskList)
      try {
        gTasks.updateTasksList(googleTaskList.title, googleTaskList.listId)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } catch (e: IOException) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun clearList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    launchDefault {
      val googleTasks =
        appDb.googleTasksDao().getAllByList(googleTaskList.listId, GTasks.TASKS_COMPLETE)
      appDb.googleTasksDao().deleteAll(googleTasks)
      gTasks.clearTaskList(googleTaskList.listId)
      postInProgress(false)
      postCommand(Commands.UPDATED)
      withUIContext {
        updatesHelper.updateTasksWidget()
      }
    }
  }

  fun saveLocalGoogleTaskList(googleTaskList: GoogleTaskList) {
    postInProgress(true)
    launchDefault {
      appDb.googleTaskListsDao().insert(googleTaskList)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
