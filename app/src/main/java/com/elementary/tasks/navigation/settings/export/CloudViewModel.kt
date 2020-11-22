package com.elementary.tasks.navigation.settings.export

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.IOException
import java.util.*

class CloudViewModel(
  private val appDb: AppDb,
  private val gTasks: GTasks
) : ViewModel(), LifecycleObserver {

  var isLoading: MutableLiveData<Boolean> = MutableLiveData()
  var isReady: MutableLiveData<Boolean> = MutableLiveData()
  val db = appDb

  private var job: Job? = null

  fun loadGoogleTasks() {
    isLoading.postValue(true)
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
          val tasksList = gTasks.getTasks(listId)
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

      withUIContext {
        isLoading.postValue(false)
        isReady.postValue(true)
      }
      job = null
    }
  }
}