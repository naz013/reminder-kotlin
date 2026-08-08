package com.github.naz013.feature.googletask.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PreviewGoogleTaskViewModel(
  private val id: String,
  private val googleTasksApi: GoogleTasksApi,
  private val dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  analyticsEventSender: AnalyticsEventSender,
  private val googleTaskPreviewStateAdapter: GoogleTaskPreviewStateAdapter,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val textProvider: TextProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(PreviewGoogleTaskState())
  val state = _state.stateInWhileSubscribed(PreviewGoogleTaskState())
    .onStart { loadTask() }
  val event: LiveData<Event<PreviewGoogleTaskEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK_PREVIEW))
  }

  fun onDeleteClick() {
    _state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    _state.update { it.copy(showDeleteConfirm = false) }
  }

  fun onDeleteConfirmed() {
    _state.update { it.copy(showDeleteConfirm = false) }
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTask = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getById(id)
      }

      if (googleTask == null) {
        event.emit(PreviewGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
        return@launch
      }

      val deleted = withContext(dispatcherProvider.io()) {
        try {
          googleTasksApi.deleteTask(googleTask).also {
            if (it) {
              googleTaskRepository.delete(googleTask.taskId)
            }
          }
        } catch (e: Throwable) {
          Logger.e(TAG, "Got an error while deleting the Google Task, ${e.message}")
          false
        }
      }

      if (deleted) {
        event.emit(PreviewGoogleTaskEvent.MoveBack)
      } else {
        event.emit(PreviewGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
      }
    }
  }

  fun onComplete() {
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTask = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getById(id)
      } ?: run {
        event.emit(PreviewGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
        return@launch
      }

      if (googleTask.isNeedAction()) {
        withContext(dispatcherProvider.io()) {
          googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, googleTask)?.also {
            googleTaskRepository.save(it)
          }
        } ?: run {
          event.emit(PreviewGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
          return@launch
        }
      } else {
        event.emit(PreviewGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
        return@launch
      }
      loadTask()
      appWidgetUpdater.updateScheduleWidget()
    }
  }

  private fun loadTask() {
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTask = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getById(id)
      } ?: run {
        Logger.w(TAG, "Google Task with id $id not found.")
        return@launch
      }
      val googleTaskList = withContext(dispatcherProvider.io()) {
        googleTaskListRepository.getById(googleTask.listId) ?: googleTaskListRepository.defaultGoogleTaskList()
      }
      val uiTask = withContext(dispatcherProvider.io()) {
        googleTaskPreviewStateAdapter.convert(googleTask, googleTaskList)
      }
      _state.update { it.copy(task = uiTask) }
    }
  }

  sealed interface PreviewGoogleTaskEvent {
    data class ShowError(
      val message: String
    ) : PreviewGoogleTaskEvent

    data object MoveBack : PreviewGoogleTaskEvent
  }

  companion object {
    private const val TAG = "PreviewGoogleTaskViewModel"
  }
}
