package com.elementary.tasks.googletasks.list

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.googletasks.usecase.tasklist.SyncGoogleTaskList
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class TaskListViewModel(
  private val listId: String,
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter,
  private val syncGoogleTaskList: SyncGoogleTaskList,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<TaskListState> field = MutableStateFlow(TaskListState())
  val navigationEvent: LiveData<Event<TaskListEvent>> field = mutableLiveEventOf()

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
      val googleTasks =
        googleTaskRepository
          .getAllByList(listId)
          .map { uiGoogleTaskListAdapter.convert(it, googleTaskList) }
      currentTaskList = googleTaskList
      val color = ThemeProvider.themedColor(contextProvider.themedContext, googleTaskList.color)
      state.update {
        it.copy(
          title = googleTaskList.title,
          tasks = googleTasks,
          fabContainerColor = Color(color),
          fabContentColor = if (color.isColorDark()) Color.White else Color.Black,
          canDelete = !googleTaskList.isDefault(),
        )
      }
    }
  }

  fun sync() {
    if (isSyncing) return
    isSyncing = true
    setBusy(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val taskList = googleTaskListRepository.getById(listId)

      if (taskList == null) {
        isSyncing = false
        withUIContext {
          setBusy(false)
          postError(textProvider.getString(R.string.failed_to_update_task))
        }
        return@launch
      }
      syncGoogleTaskList(taskList)

      load()

      isSyncing = false

      withUIContext {
        setBusy(false)
        appWidgetUpdater.updateScheduleWidget()
      }
    }
  }

  fun clearList() {
    if (isSyncing) return
    val googleTaskList = currentTaskList ?: return
    setBusy(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTasks =
        googleTaskRepository.getAllByList(googleTaskList.listId, GoogleTask.TASKS_COMPLETE)
      googleTaskRepository.deleteAll(googleTasks.map { it.taskId })
      googleTasksApi.clearTaskList(googleTaskList.listId)
      load()
      setBusy(false)
      withUIContext {
        appWidgetUpdater.updateScheduleWidget()
      }
    }
  }

  fun onDeleteListClick() {
    state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    state.update { it.copy(showDeleteConfirm = false) }
  }

  fun deleteGoogleTaskList() {
    if (isSyncing) return
    val googleTaskList = currentTaskList ?: return
    state.update { it.copy(showDeleteConfirm = false) }
    setBusy(true)
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
        setBusy(false)
        navigationEvent.postValue(Event(TaskListEvent.Deleted))
      } else {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  fun toggleTask(taskId: String) {
    if (isSyncing) return
    setBusy(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTaskRepository.getById(taskId)
        if (googleTask == null) {
          setBusy(false)
          postError(textProvider.getString(R.string.failed_to_update_task))
          return@launch
        }
        val updated =
          if (googleTask.isNeedAction()) {
            googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, googleTask)
          } else {
            googleTasksApi.updateTaskStatus(GoogleTask.TASKS_NEED_ACTION, googleTask)
          }
        updated?.let { googleTaskRepository.save(it) }
        load()
        setBusy(false)
        withUIContext {
          appWidgetUpdater.updateScheduleWidget()
        }
      } catch (e: IOException) {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun setBusy(busy: Boolean) {
    postInProgress(busy)
    state.update { it.copy(isLoading = busy) }
  }
}
