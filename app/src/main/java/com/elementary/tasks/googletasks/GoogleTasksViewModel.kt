package com.elementary.tasks.googletasks

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.googletasks.usecase.tasklist.SyncAllGoogleTaskLists
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class GoogleTasksViewModel(
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
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
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTaskLists = googleTaskListRepository.getAll()

      val map = mapTaskLists(googleTaskLists)

      val googleTasks = googleTaskRepository.getAll().map {
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
        updated?.also { googleTaskRepository.save(it) }
        load()
        postInProgress(false)
        postCommand(Commands.UPDATED)
        withUIContext {
          appWidgetUpdater.updateScheduleWidget()
        }
      } catch (e: IOException) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }
}
