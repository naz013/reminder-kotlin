package com.elementary.tasks.core.view_models.google_tasks

import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.IOException
import java.util.*

class GoogleTaskListsViewModel : BaseTaskListsViewModel() {

  val googleTaskLists = appDb.googleTaskListsDao().loadAll()
  val allGoogleTasks = appDb.googleTasksDao().loadAll()
  val defTaskList = appDb.googleTaskListsDao().loadDefault()
  private var isSyncing = false
  private var job: Job? = null

  init {
    defTaskList.observeForever { }
  }

  fun loadGoogleTasks() {
    postInProgress(true)
    job = launchDefault {
      GTasks.getInstance(context)?.let { tasks ->
        var lists: TaskLists? = null
        try {
          lists = tasks.taskLists()
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
            Timber.d("loadGoogleTasks: $taskList")
            appDb.googleTaskListsDao().insert(taskList)
            val tasksList = tasks.getTasks(listId)
            if (tasksList.isNotEmpty()) {
              for (task in tasksList) {
                var googleTask = appDb.googleTasksDao().getById(task.id)
                if (googleTask != null) {
                  googleTask.update(task)
                  googleTask.listId = task.id
                } else {
                  googleTask = GoogleTask(task, listId)
                }
                appDb.googleTasksDao().insert(googleTask)
              }
            }
          }
          val local = appDb.googleTaskListsDao().all()
          if (local.isNotEmpty()) {
            val listItem = local[0].apply {
              this.def = 1
              this.systemDefault = 1
            }
            appDb.googleTaskListsDao().insert(listItem)
          }
        }
      }

      withUIContext {
        postInProgress(false)
      }
      job = null
    }
  }

  fun sync() {
    val google = GTasks.getInstance(context)
    if (google == null) {
      postCommand(Commands.FAILED)
      return
    }
    if (isSyncing) return
    isSyncing = true
    postInProgress(true)
    launchDefault {
      var lists: TaskLists? = null
      try {
        lists = google.taskLists()
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
          val tasks = google.getTasks(listId)
          if (tasks.isEmpty()) {
            withUIContext {
              postInProgress(false)
              postCommand(Commands.UPDATED)
              UpdatesHelper.updateTasksWidget(context)
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
              UpdatesHelper.updateTasksWidget(context)
            }
          }
        }
      }
      isSyncing = false
    }
  }
}
