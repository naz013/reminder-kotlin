package com.elementary.tasks.googletasks.tasklist

import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.launch
import java.io.IOException

class GoogleTaskListViewModel(
  listId: String,
  private val gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val analyticsEventSender: AnalyticsEventSender
) : BaseProgressViewModel(dispatcherProvider) {

  var googleTaskList = googleTaskListsDao.loadById(listId)
  var googleTask = googleTasksDao.loadAllByList(listId)

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
      googleTaskListsDao.delete(googleTaskList)
      googleTasksDao.deleteAll(googleTaskList.listId)
      if (def == 1) {
        val lists = googleTaskListsDao.all()
        if (lists.isNotEmpty()) {
          val taskList = lists[0]
          taskList.def = 1
          googleTaskListsDao.insert(taskList)
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
        val default = googleTaskListsDao.getDefault()
        default.forEach {
          it.def = 0
          googleTaskListsDao.insert(it)
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
        val default = googleTaskListsDao.getDefault()
        default.forEach {
          it.def = 0
          googleTaskListsDao.insert(it)
        }
      }
      googleTaskListsDao.insert(googleTaskList)
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
