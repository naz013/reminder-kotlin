package com.elementary.tasks.googletasks.list

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.googletasks.usecase.tasklist.SyncGoogleTaskList
import kotlinx.coroutines.launch
import java.io.IOException

class TaskListViewModel(
  private val listId: String,
  private val gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  private val updatesHelper: UpdatesHelper,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter,
  private val syncGoogleTaskList: SyncGoogleTaskList
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
      val taskList = googleTaskListsDao.getById(listId)

      if (taskList == null) {
        withUIContext {
          postInProgress(false)
          postCommand(Commands.FAILED)
        }
        return@launch
      }
      syncGoogleTaskList(taskList)

      load()

      isSyncing = false

      withUIContext {
        postInProgress(false)
        postCommand(Commands.UPDATED)
        updatesHelper.updateTasksWidget()
      }
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
