package com.github.naz013.feature.googletask

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.feature.googletask.usecase.tasklist.SyncGoogleTaskList
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.ui.common.compose.toColor
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class TaskListViewModel(
  private val listId: String,
  private val googleTasksApi: GoogleTasksApi,
  private val dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskItemStateAdapter: GoogleTaskItemStateAdapter,
  private val syncGoogleTaskList: SyncGoogleTaskList,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(TaskListState())
  val state = _state.stateInWhileSubscribed(TaskListState())
    .onStart { load() }

  val event: LiveData<Event<TaskListEvent>> field = mutableLiveEventOf()

  fun onEditClicked() {
    event.emit(TaskListEvent.EditTaskList(_state.value.listId))
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTaskList = withContext(dispatcherProvider.io()) {
        googleTaskListRepository.getById(listId)
      } ?: run {
        return@launch
      }
      val googleTasks = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getAllByList(listId).map {
          googleTaskItemStateAdapter.convert(it, googleTaskList)
        }
      }
      val color = ThemeProvider.themedColor(contextProvider.themedContext, googleTaskList.color)
      _state.update {
        it.copy(
          listId = googleTaskList.listId,
          title = googleTaskList.title,
          tasks = googleTasks,
          fabContainerColor = color.toColor(),
          fabContentColor = if (color.isColorDark()) Color.White else Color.Black,
          canDelete = !googleTaskList.isDefault(),
          isDefaultList = googleTaskList.isDefault(),
        )
      }
    }
  }

  fun sync() {
    if (_state.value.isSyncing) return
    _state.update {
      it.copy(isSyncing = true)
    }
    viewModelScope.launch(dispatcherProvider.main()) {
      val taskList = withContext(dispatcherProvider.io()) {
        googleTaskListRepository.getById(listId)
      } ?: run {
        _state.update {
          it.copy(isSyncing = false)
        }
        event.emit(TaskListEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
        return@launch
      }

      withContext(dispatcherProvider.io()) {
        syncGoogleTaskList(taskList)
      }

      load()
      appWidgetUpdater.updateScheduleWidget()

      _state.update {
        it.copy(isSyncing = false)
      }
    }
  }

  fun clearList() {
    if (_state.value.isSyncing) return
    _state.update {
      it.copy(isSyncing = true)
    }
    viewModelScope.launch(dispatcherProvider.main()) {
      withContext(dispatcherProvider.io()) {
        val googleTasks = googleTaskRepository.getAllByList(_state.value.listId, GoogleTask.TASKS_COMPLETE)
        googleTaskRepository.deleteAll(googleTasks.map { it.taskId })
        googleTasksApi.clearTaskList(_state.value.listId)
      }

      load()
      appWidgetUpdater.updateScheduleWidget()

      _state.update {
        it.copy(isSyncing = false)
      }
    }
  }

  fun onDeleteListClick() {
    _state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    _state.update { it.copy(showDeleteConfirm = false) }
  }

  fun deleteGoogleTaskList() {
    if (_state.value.isSyncing) return
    _state.update {
      it.copy(
        isSyncing = true,
        showDeleteConfirm = false
      )
    }
    viewModelScope.launch(dispatcherProvider.main()) {
      val deleted = withContext(dispatcherProvider.io()) {
        googleTasksApi.deleteTaskList(_state.value.listId).also {
          if (it) {
            googleTaskListRepository.delete(_state.value.listId)
            googleTaskRepository.deleteAll(_state.value.listId)
            if (_state.value.isDefaultList) {
              googleTaskListRepository.getAll().firstOrNull()?.also { list ->
                list.def = 1
                googleTaskListRepository.save(list)
              }
            }
          }
        }
      }
      if (deleted) {
        event.emit(TaskListEvent.MoveBack)
      } else {
        _state.update {
          it.copy(isSyncing = false)
        }
        event.emit(TaskListEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
      }
    }
  }

  fun toggleTask(taskId: String) {
    if (_state.value.isSyncing) return
    _state.update {
      it.copy(isSyncing = true)
    }
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTask = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getById(taskId)
      } ?: run {
        _state.update {
          it.copy(isSyncing = false)
        }
        event.emit(TaskListEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
        return@launch
      }
      val updated = withContext(dispatcherProvider.io()) {
        try {
          val savedGoogleTask = if (googleTask.isNeedAction()) {
            googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, googleTask)
          } else {
            googleTasksApi.updateTaskStatus(GoogleTask.TASKS_NEED_ACTION, googleTask)
          }
          savedGoogleTask?.also { googleTaskRepository.save(it) }
          savedGoogleTask != null
        } catch (e: Exception) {
          Logger.e(TAG, "Failed to update the Google Task List, error: ${e.message}")
          false
        }
      }

      if (updated) {
        load()
        appWidgetUpdater.updateScheduleWidget()
      } else {
        event.emit(TaskListEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
      }

      _state.update {
        it.copy(isSyncing = false)
      }
    }
  }

  sealed interface TaskListEvent {
    data object MoveBack : TaskListEvent

    data class ShowError(
      val message: String
    ) : TaskListEvent

    data class EditTaskList(
      val listId: String
    ) : TaskListEvent
  }

  companion object {
    private const val TAG = "TaskListViewModel"
  }
}
