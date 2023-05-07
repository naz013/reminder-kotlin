package com.elementary.tasks.settings.export

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.factory.GoogleTaskFactory
import com.elementary.tasks.core.data.factory.GoogleTaskListFactory
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.withUIContext
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.util.Random

class CloudViewModel(
  private val appDb: AppDb,
  private val gTasks: GTasks,
  private val dispatcherProvider: DispatcherProvider,
  private val updatesHelper: UpdatesHelper,
  private val googleTaskFactory: GoogleTaskFactory,
  private val googleTaskListFactory: GoogleTaskListFactory
) : ViewModel(), LifecycleObserver {

  var isLoading: MutableLiveData<Boolean> = MutableLiveData()

  fun clearGoogleTasks() {
    isLoading.postValue(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      appDb.googleTasksDao().deleteAll()
      appDb.googleTaskListsDao().deleteAll()
      withUIContext {
        updatesHelper.updateTasksWidget()
        isLoading.postValue(false)
      }
    }
  }

  fun loadGoogleTasks() {
    isLoading.postValue(true)
    viewModelScope.launch(dispatcherProvider.default()) {
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
          taskList = if (taskList != null) {
            googleTaskListFactory.update(taskList, item)
          } else {
            val r = Random()
            val color = r.nextInt(15)
            googleTaskListFactory.create(item, color)
          }
          Timber.d("loadGoogleTasks: $taskList")
          appDb.googleTaskListsDao().insert(taskList)
          val tasksList = gTasks.getTasks(listId)
          if (tasksList.isNotEmpty()) {
            for (task in tasksList) {
              var googleTask = appDb.googleTasksDao().getById(task.id)
              if (googleTask != null) {
                googleTask = googleTaskFactory.update(googleTask, task)
                googleTask.listId = task.id
              } else {
                googleTask = googleTaskFactory.create(task, listId)
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
        updatesHelper.updateTasksWidget()
      }
    }
  }
}