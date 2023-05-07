package com.elementary.tasks.google_tasks.tasklist

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.factory.GoogleTaskFactory
import com.elementary.tasks.core.data.factory.GoogleTaskListFactory
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.withUIContext
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Random

class GoogleTaskListViewModel(
  listId: String,
  private val gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  private val updatesHelper: UpdatesHelper,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val analyticsEventSender: AnalyticsEventSender,
  private val googleTaskFactory: GoogleTaskFactory,
  private val googleTaskListFactory: GoogleTaskListFactory
) : BaseProgressViewModel(dispatcherProvider) {

  var googleTaskList = googleTaskListsDao.loadById(listId)
  var googleTask = googleTasksDao.loadAllByList(listId)

  var isEdited = false
  var listId: String = ""
  var action: String = ""

  var isLoading = false
  var editedTaskList: GoogleTaskList? = null

  private var isSyncing = false

  fun canDelete(): Boolean {
    return editedTaskList?.let { !it.isDefault() } ?: false
  }

  fun deleteGoogleTaskList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
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

  fun sync() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    if (isSyncing) return
    isSyncing = true
    postInProgress(true)
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
          var taskList = googleTaskListsDao.getById(listId)
          taskList = if (taskList != null) {
            googleTaskListFactory.update(taskList, item)
          } else {
            val r = Random()
            val color = r.nextInt(15)
            googleTaskListFactory.create(item, color)
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
                googleTask = googleTaskFactory.update(googleTask, task)
              } else {
                googleTask = googleTaskFactory.create(task, listId)
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
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTaskList.isDefault()) {
        val default = googleTaskListsDao.getDefault()
        default.forEach {
          it.def = 0
          googleTaskListsDao.insert(it)
        }
      }
      gTasks.insertTasksList(googleTaskList.title, googleTaskList.color)
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK_LIST))
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
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTaskList.isDefault()) {
        val default = googleTaskListsDao.getDefault()
        default.forEach {
          it.def = 0
          googleTaskListsDao.insert(it)
        }
      }
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
}
