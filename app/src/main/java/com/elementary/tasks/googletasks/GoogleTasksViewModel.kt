package com.elementary.tasks.googletasks

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
import com.elementary.tasks.googletasks.usecase.tasklist.SyncAllGoogleTaskLists
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class GoogleTasksViewModel(
  private val gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  private val updatesHelper: UpdatesHelper,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter,
  private val syncAllGoogleTaskLists: SyncAllGoogleTaskLists
) : BaseProgressViewModel(dispatcherProvider) {

  private val _googleTaskLists = mutableLiveDataOf<List<GoogleTaskList>>()
  val googleTaskLists = _googleTaskLists.toLiveData()

  private val _allGoogleTasks = mutableLiveDataOf<List<UiGoogleTaskList>>()
  val allGoogleTasks = _allGoogleTasks.toLiveData()

  private val _defTaskList = mutableLiveDataOf<GoogleTaskList>()
  val defTaskList = _defTaskList.toLiveData()

  private var isSyncing = false
  private var job: Job? = null

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    load()
  }

  private fun load() {
    if (!gTasks.isLogged) {
      return
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTaskLists = googleTaskListsDao.all()

      val map = mapTaskLists(googleTaskLists)

      val googleTasks = googleTasksDao.all().map {
        uiGoogleTaskListAdapter.convert(it, map[it.listId])
      }

      _defTaskList.postValue(googleTaskLists.firstOrNull { it.isDefault() })
      _googleTaskLists.postValue(googleTaskLists)
      _allGoogleTasks.postValue(googleTasks)
    }
  }

  private fun mapTaskLists(list: List<GoogleTaskList>): Map<String, GoogleTaskList> {
    val map = mutableMapOf<String, GoogleTaskList>()
    list.forEach { map[it.listId] = it }
    return map
  }

  fun loadGoogleTasks() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    job = viewModelScope.launch(dispatcherProvider.default()) {
      syncAllGoogleTaskLists()
      load()
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
    viewModelScope.launch(dispatcherProvider.default()) {
      syncAllGoogleTaskLists()
      load()
      isSyncing = false
      withUIContext {
        postInProgress(false)
      }
    }
  }

  fun toggleTask(taskId: String) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
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
