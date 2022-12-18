package com.elementary.tasks.core.view_models.google_tasks

import androidx.lifecycle.LiveData
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
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
  gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  updatesHelper: UpdatesHelper,
  googleTasksDao: GoogleTasksDao,
  googleTaskListsDao: GoogleTaskListsDao
) : BaseTaskListsViewModel(
  gTasks,
  dispatcherProvider,
  updatesHelper,
  googleTasksDao,
  googleTaskListsDao
) {

  var googleTaskList: LiveData<GoogleTaskList>
  val defaultTaskList = googleTaskListsDao.loadDefault()
  var googleTasks: LiveData<List<GoogleTask>>
  private var isSyncing = false

  init {
    Timber.d("GoogleTaskListViewModel: $listId")
    googleTaskList = googleTaskListsDao.loadById(listId)
    googleTasks = googleTasksDao.loadAllByList(listId)
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
          var taskList = googleTaskListsDao.getById(listId)
          if (taskList != null) {
            taskList.update(item)
          } else {
            val r = Random()
            val color = r.nextInt(15)
            taskList = GoogleTaskList(item, color)
          }
          googleTaskListsDao.insert(taskList)
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
              var googleTask = googleTasksDao.getById(task.id)
              if (googleTask != null) {
                googleTask.listId = listId
                googleTask.update(task)
              } else {
                googleTask = GoogleTask(task, listId)
              }
              googleTasks.add(googleTask)
            }
            googleTasksDao.insertAll(googleTasks)
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
      googleTaskListsDao.insert(googleTaskList)
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
      val googleTasks = googleTasksDao.getAllByList(googleTaskList.listId, GTasks.TASKS_COMPLETE)
      googleTasksDao.deleteAll(googleTasks)
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
      googleTaskListsDao.insert(googleTaskList)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
