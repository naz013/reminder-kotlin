package com.elementary.tasks.googletasks.preview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskPreviewAdapter
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview
import com.elementary.tasks.core.utils.withUIContext
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import kotlinx.coroutines.launch

class GoogleTaskPreviewViewModel(
  private val id: String,
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiGoogleTaskPreviewAdapter: UiGoogleTaskPreviewAdapter,
  private val appWidgetUpdater: AppWidgetUpdater
) : BaseProgressViewModel(dispatcherProvider) {

  private val _googleTask = mutableLiveDataOf<UiGoogleTaskPreview>()
  val googleTask = _googleTask.toLiveData()

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK_PREVIEW))
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadTask()
  }

  fun onDelete() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTaskRepository.getById(id)
        if (googleTask == null) {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        if (googleTasksApi.deleteTask(googleTask)) {
          googleTaskRepository.delete(googleTask.taskId)
          postInProgress(false)
          postCommand(Commands.DELETED)
        } else {
          postInProgress(false)
          postCommand(Commands.FAILED)
        }
      } catch (e: Throwable) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun onComplete() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTaskRepository.getById(id)
        if (googleTask == null) {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        if (googleTask.isNeedAction()) {
          googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, googleTask)?.also {
            googleTaskRepository.save(it)
          } ?: run {
            postInProgress(false)
            postCommand(Commands.FAILED)
            return@launch
          }
        } else {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        loadTask()
        postInProgress(false)
        postCommand(Commands.UPDATED)
        withUIContext {
          appWidgetUpdater.updateScheduleWidget()
        }
      } catch (e: Throwable) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun loadTask() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTask = googleTaskRepository.getById(id) ?: return@launch
      val googleTaskList = googleTaskListRepository.getById(googleTask.listId)
        ?: googleTaskListRepository.defaultGoogleTaskList()
      _googleTask.postValue(uiGoogleTaskPreviewAdapter.convert(googleTask, googleTaskList))
    }
  }
}
