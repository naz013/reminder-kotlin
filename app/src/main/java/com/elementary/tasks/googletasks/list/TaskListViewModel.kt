package com.elementary.tasks.googletasks.list

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.googletasks.usecase.tasklist.SyncGoogleTaskList
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import kotlinx.coroutines.launch
import java.io.IOException

class TaskListViewModel(
  private val listId: String,
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val updatesHelper: UpdatesHelper,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
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
      val googleTaskList = googleTaskListRepository.getById(listId) ?: return@launch
      val googleTasks = googleTaskRepository.getAllByList(listId).map {
        uiGoogleTaskListAdapter.convert(it, googleTaskList)
      }
      currentTaskList = googleTaskList
      _taskList.postValue(googleTaskList)
      _tasks.postValue(googleTasks)
    }
  }

  fun sync() {
    if (isSyncing) return
    isSyncing = true
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val taskList = googleTaskListRepository.getById(listId)

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
    if (isSyncing) return
    val googleTaskList = currentTaskList ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTasks =
        googleTaskRepository.getAllByList(googleTaskList.listId, GoogleTask.TASKS_COMPLETE)
      googleTaskRepository.deleteAll(googleTasks.map { it.taskId })
      googleTasksApi.clearTaskList(googleTaskList.listId)
      load()
      postInProgress(false)
      postCommand(Commands.UPDATED)
      withUIContext {
        updatesHelper.updateTasksWidget()
      }
    }
  }

  fun deleteGoogleTaskList() {
    if (isSyncing) return
    val googleTaskList = currentTaskList ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTasksApi.deleteTaskList(googleTaskList.listId)) {
        googleTaskListRepository.delete(googleTaskList.listId)
        googleTaskRepository.deleteAll(googleTaskList.listId)
        if (googleTaskList.def == 1) {
          googleTaskListRepository.getAll().firstOrNull()?.also {
            it.def = 1
            googleTaskListRepository.save(it)
          }
        }
        load()
        postInProgress(false)
        postCommand(Commands.DELETED)
      } else {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun toggleTask(taskId: String) {
    if (isSyncing) return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTaskRepository.getById(taskId)
        if (googleTask == null) {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        val updated = if (googleTask.isNeedAction()) {
          googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, googleTask)
        } else {
          googleTasksApi.updateTaskStatus(GoogleTask.TASKS_NEED_ACTION, googleTask)
        }
        updated?.let { googleTaskRepository.save(it) }
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
