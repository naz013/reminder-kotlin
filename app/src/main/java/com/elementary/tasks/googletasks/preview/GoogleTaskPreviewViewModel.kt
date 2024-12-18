package com.elementary.tasks.googletasks.preview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskPreviewAdapter
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.launch

class GoogleTaskPreviewViewModel(
    private val id: String,
    private val gTasks: GTasks,
    dispatcherProvider: DispatcherProvider,
    private val googleTasksDao: GoogleTasksDao,
    private val googleTaskListsDao: GoogleTaskListsDao,
    private val analyticsEventSender: AnalyticsEventSender,
    private val uiGoogleTaskPreviewAdapter: UiGoogleTaskPreviewAdapter,
    private val updatesHelper: UpdatesHelper
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
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTasksDao.getById(id)
        if (googleTask == null) {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        gTasks.deleteTask(googleTask)
        googleTasksDao.delete(googleTask)
        postInProgress(false)
        postCommand(Commands.DELETED)
      } catch (e: Throwable) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun onComplete() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTasksDao.getById(id)
        if (googleTask == null) {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        if (googleTask.status == GTasks.TASKS_NEED_ACTION) {
          gTasks.updateTaskStatus(GTasks.TASKS_COMPLETE, googleTask)
        } else {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        loadTask()
        postInProgress(false)
        postCommand(Commands.UPDATED)
        withUIContext {
          updatesHelper.updateTasksWidget()
        }
      } catch (e: Throwable) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun loadTask() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTask = googleTasksDao.getById(id) ?: return@launch
      val googleTaskList = googleTaskListsDao.getById(googleTask.listId)
        ?: googleTaskListsDao.defaultGoogleTaskList()
      _googleTask.postValue(uiGoogleTaskPreviewAdapter.convert(googleTask, googleTaskList))
    }
  }
}
