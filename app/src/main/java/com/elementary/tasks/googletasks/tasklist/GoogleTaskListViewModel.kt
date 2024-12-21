package com.elementary.tasks.googletasks.tasklist

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.observeTable
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.launch
import java.io.IOException

class GoogleTaskListViewModel(
  listId: String,
  private val gTasks: GTasks,
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

  private var isSyncing = false

  fun canDelete(): Boolean {
    return editedTaskList?.let { !it.isDefault() } ?: false
  }

  fun deleteGoogleTaskList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val def = googleTaskList.def
      gTasks.deleteTaskList(googleTaskList.listId)
      googleTaskListRepository.delete(googleTaskList.listId)
      googleTaskRepository.deleteAll(googleTaskList.listId)
      if (def == 1) {
        val lists = googleTaskListRepository.getAll()
        if (lists.isNotEmpty()) {
          val taskList = lists[0]
          taskList.def = 1
          googleTaskListRepository.save(taskList)
        }
      }
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun newGoogleTaskList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTaskList.isDefault()) {
        val default = googleTaskListRepository.getDefault()
        default.forEach {
          it.def = 0
          googleTaskListRepository.save(it)
        }
      }
      gTasks.insertTasksList(googleTaskList.title, googleTaskList.color)
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK_LIST))
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun updateGoogleTaskList(googleTaskList: GoogleTaskList) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTaskList.isDefault()) {
        val default = googleTaskListRepository.getDefault()
        default.forEach {
          it.def = 0
          googleTaskListRepository.save(it)
        }
      }
      googleTaskListRepository.save(googleTaskList)
      try {
        gTasks.updateTasksList(googleTaskList.title, googleTaskList.listId)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } catch (e: IOException) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }
}
