package com.elementary.tasks.googletasks.tasklist

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.usecase.googletasks.GetGoogleTaskListByIdUseCase
import kotlinx.coroutines.launch

class EditGoogleTaskListViewModel(
  arguments: Bundle?,
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  private val getGoogleTaskListByIdUseCase: GetGoogleTaskListByIdUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  private val _googleTaskList = mutableLiveDataOf<GoogleTaskList>()
  val googleTaskList = _googleTaskList.toLiveData()

  private val _colorChanged = mutableLiveDataOf<Int>()
  val colorChanged = _colorChanged.toLiveData()

  var isEdited = false
    private set
  var listId: String = ""
    private set

  private var color: Int = 0
  var editedTaskList: GoogleTaskList? = null

  init {
    val id = arguments?.getString(IntentKeys.INTENT_ID) ?: ""
    listId = id
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      editedTaskList = getGoogleTaskListByIdUseCase(id)
      editedTaskList?.also {
        _googleTaskList.postValue(it)
      }
      postInProgress(false)
    }
  }

  fun onColorChanged(color: Int) {
    this.color = color
  }

  fun hasId(): Boolean {
    return listId.isNotEmpty()
  }

  fun onCreated(savedInstanceState: Bundle?) {
    if (savedInstanceState != null) {
      _colorChanged.postValue(color)
    }
  }

  fun canDelete(): Boolean {
    return editedTaskList?.let { !it.isDefault() } ?: false
  }

  fun deleteGoogleTaskList() {
    val googleTaskList = editedTaskList ?: return
    postInProgress(true)
    Logger.i(TAG, "Deleting Google Task List (${googleTaskList.listId})")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTasksApi.deleteTaskList(googleTaskList.listId)) {
        googleTaskListRepository.delete(googleTaskList.listId)
        googleTaskRepository.deleteAll(googleTaskList.listId)
        if (googleTaskList.def == 1) {
          val lists = googleTaskListRepository.getAll()
          if (lists.isNotEmpty()) {
            val taskList = lists[0]
            taskList.def = 1
            googleTaskListRepository.save(taskList)
          }
        }
        postInProgress(false)
        postCommand(Commands.DELETED)
      } else {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun newGoogleTaskList(googleTaskList: GoogleTaskList) {
    postInProgress(true)
    Logger.i(
      TAG,
      "Creating Google Task List (${googleTaskList.listId}), default=${googleTaskList.isDefault()}"
    )
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTaskList.isDefault()) {
        googleTaskListRepository.getDefault().forEach {
          it.def = 0
          googleTaskListRepository.save(it)
        }
      }
      googleTasksApi.saveTasksList(googleTaskList.title, googleTaskList.color)?.let {
        googleTaskListRepository.save(it)
        analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK_LIST))
        postInProgress(false)
        postCommand(Commands.SAVED)
      } ?: run {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun updateGoogleTaskList(googleTaskList: GoogleTaskList) {
    postInProgress(true)
    Logger.i(TAG, "Updating Google Task List (${googleTaskList.listId})")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTaskList.isDefault()) {
        googleTaskListRepository.getDefault().forEach {
          it.def = 0
          googleTaskListRepository.save(it)
        }
      }
      googleTasksApi.updateTasksList(googleTaskList.title, googleTaskList)?.let {
        googleTaskListRepository.save(it)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } ?: run {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  companion object {
    private const val TAG = "EditGoogleTaskViewModel"
  }
}
