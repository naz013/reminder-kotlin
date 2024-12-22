package com.elementary.tasks.googletasks.tasklist

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.observeTable
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.launch

class GoogleTaskListViewModel(
  listId: String,
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  tableChangeListenerFactory: TableChangeListenerFactory
) : BaseProgressViewModel(dispatcherProvider) {

  var googleTaskList = viewModelScope.observeTable(
    table = Table.GoogleTaskList,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { googleTaskListRepository.getById(listId) }
  )
  var googleTask = viewModelScope.observeTable(
    table = Table.GoogleTask,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { googleTaskRepository.getAllByList(listId) }
  )

  var isEdited = false
  var listId: String = ""
  var action: String = ""

  var isLoading = false
  var editedTaskList: GoogleTaskList? = null

  fun canDelete(): Boolean {
    return editedTaskList?.let { !it.isDefault() } ?: false
  }

  fun deleteGoogleTaskList(googleTaskList: GoogleTaskList) {
    postInProgress(true)
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
}
