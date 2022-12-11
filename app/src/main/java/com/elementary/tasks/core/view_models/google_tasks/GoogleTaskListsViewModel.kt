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
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.IOException
import java.util.Random

class GoogleTaskListsViewModel(
  prefs: Prefs,
  context: Context,
  gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  updatesHelper: UpdatesHelper,
  googleTasksDao: GoogleTasksDao,
  googleTaskListsDao: GoogleTaskListsDao
) : BaseTaskListsViewModel(
  prefs,
  context,
  gTasks,
  dispatcherProvider,
  workManagerProvider,
  updatesHelper,
  googleTasksDao,
  googleTaskListsDao
) {

  val googleTaskLists = googleTaskListsDao.loadAll()
  val allGoogleTasks = googleTasksDao.loadAll()
  val defTaskList = googleTaskListsDao.loadDefault()
  private var isSyncing = false
  private var job: Job? = null

  init {
    defTaskList.observeForever { }
  }

  fun loadGoogleTasks() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    job = launchDefault {
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
          Timber.d("loadGoogleTasks: $taskList")
          googleTaskListsDao.insert(taskList)
          val tasksList = gTasks.getTasks(listId)
          if (tasksList.isNotEmpty()) {
            for (task in tasksList) {
              var googleTask = googleTasksDao.getById(task.id)
              if (googleTask != null) {
                googleTask.update(task)
                googleTask.listId = task.id
              } else {
                googleTask = GoogleTask(task, listId)
              }
              googleTasksDao.insert(googleTask)
            }
          }
        }
        val local = googleTaskListsDao.all()
        if (local.isNotEmpty()) {
          val listItem = local[0].apply {
            this.def = 1
            this.systemDefault = 1
          }
          googleTaskListsDao.insert(listItem)
        }
      }

      withUIContext {
        postInProgress(false)
      }
      job = null
    }
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
}
