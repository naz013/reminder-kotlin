package com.elementary.tasks.googletasks.preview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskPreviewAdapter
import com.elementary.tasks.core.utils.withUIContext
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreviewGoogleTaskViewModel(
  private val id: String,
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiGoogleTaskPreviewAdapter: UiGoogleTaskPreviewAdapter,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val textProvider: TextProvider,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<PreviewGoogleTaskState> field = MutableStateFlow(PreviewGoogleTaskState())
  val navigationEvent: LiveData<Event<PreviewGoogleTaskEvent>> field = mutableLiveEventOf()

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK_PREVIEW))
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadTask()
  }

  fun onDeleteClick() {
    state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    state.update { it.copy(showDeleteConfirm = false) }
  }

  fun onDeleteConfirmed() {
    state.update { it.copy(showDeleteConfirm = false) }
    setBusy(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTaskRepository.getById(id)
        if (googleTask == null) {
          setBusy(false)
          postError(textProvider.getString(R.string.failed_to_update_task))
          return@launch
        }
        if (googleTasksApi.deleteTask(googleTask)) {
          googleTaskRepository.delete(googleTask.taskId)
          setBusy(false)
          navigationEvent.postValue(Event(PreviewGoogleTaskEvent.Deleted))
        } else {
          setBusy(false)
          postError(textProvider.getString(R.string.failed_to_update_task))
        }
      } catch (e: Throwable) {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  fun onComplete() {
    setBusy(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTaskRepository.getById(id)
        if (googleTask == null) {
          setBusy(false)
          postError(textProvider.getString(R.string.failed_to_update_task))
          return@launch
        }
        if (googleTask.isNeedAction()) {
          googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, googleTask)?.also {
            googleTaskRepository.save(it)
          } ?: run {
            setBusy(false)
            postError(textProvider.getString(R.string.failed_to_update_task))
            return@launch
          }
        } else {
          setBusy(false)
          postError(textProvider.getString(R.string.failed_to_update_task))
          return@launch
        }
        loadTask()
        setBusy(false)
        withUIContext {
          appWidgetUpdater.updateScheduleWidget()
        }
      } catch (e: Throwable) {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun loadTask() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTask = googleTaskRepository.getById(id) ?: return@launch
      val googleTaskList =
        googleTaskListRepository.getById(googleTask.listId)
          ?: googleTaskListRepository.defaultGoogleTaskList()
      val uiTask = uiGoogleTaskPreviewAdapter.convert(googleTask, googleTaskList)
      state.update { it.copy(task = uiTask) }
    }
  }

  private fun setBusy(busy: Boolean) {
    postInProgress(busy)
    state.update { it.copy(isLoading = busy) }
  }
}
