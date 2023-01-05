package com.elementary.tasks.google_tasks.list

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Random

class TaskListViewModel(
  private val listId: String,
  private val gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  private val updatesHelper: UpdatesHelper,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val _taskList = mutableLiveDataOf<GoogleTaskList>()
  val taskList = _taskList.toLiveData()

  private val _tasks = mutableLiveDataOf<List<UiGoogleTaskList>>()
  val tasks = _tasks.toLiveData()

  private var isSyncing = false
  var currentTaskList: GoogleTaskList? = null
    private set

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    load()
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTaskList = googleTaskListsDao.getById(listId) ?: return@launch
      val googleTasks = googleTasksDao.getAllByList(listId).map {
        uiGoogleTaskListAdapter.convert(it, googleTaskList)
      }
      currentTaskList = googleTaskList
      _taskList.postValue(googleTaskList)
      _tasks.postValue(googleTasks)
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

        val local = googleTaskListsDao.all()
        val hasDefault = local.firstOrNull { it.isDefault() }
        if (hasDefault == null) {
          val listItem = local[0].apply {
            this.def = 1
            this.systemDefault = 1
          }
          googleTaskListsDao.insert(listItem)
        }
      }
      isSyncing = false
    }
  }

  fun clearList() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    if (isSyncing) return
    val googleTaskList = currentTaskList ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTasks = googleTasksDao.getAllByList(googleTaskList.listId, GTasks.TASKS_COMPLETE)
      googleTasksDao.deleteAll(googleTasks)
      gTasks.clearTaskList(googleTaskList.listId)
      load()
      postInProgress(false)
      postCommand(Commands.UPDATED)
      withUIContext {
        updatesHelper.updateTasksWidget()
      }
    }
  }

  fun deleteGoogleTaskList() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    if (isSyncing) return
    val googleTaskList = currentTaskList ?: return
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
      load()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun toggleTask(taskId: String) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    if (isSyncing) return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTasksDao.getById(taskId)
        if (googleTask == null) {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        if (googleTask.status == GTasks.TASKS_NEED_ACTION) {
          gTasks.updateTaskStatus(GTasks.TASKS_COMPLETE, googleTask)
        } else {
          gTasks.updateTaskStatus(GTasks.TASKS_NEED_ACTION, googleTask)
        }
        load()
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
