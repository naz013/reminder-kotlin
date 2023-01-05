package com.elementary.tasks.google_tasks

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.util.Random

class GoogleTasksViewModel(
  private val gTasks: GTasks,
  dispatcherProvider: DispatcherProvider,
  private val updatesHelper: UpdatesHelper,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val _googleTaskLists = mutableLiveDataOf<List<GoogleTaskList>>()
  val googleTaskLists = _googleTaskLists.toLiveData()

  private val _allGoogleTasks = mutableLiveDataOf<List<UiGoogleTaskList>>()
  val allGoogleTasks = _allGoogleTasks.toLiveData()

  private val _defTaskList = mutableLiveDataOf<GoogleTaskList>()
  val defTaskList = _defTaskList.toLiveData()

  private var isSyncing = false
  private var job: Job? = null

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    load()
  }

  private fun load() {
    if (!gTasks.isLogged) {
      return
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTaskLists = googleTaskListsDao.all()

      val map = mapTaskLists(googleTaskLists)

      val googleTasks = googleTasksDao.all().map {
        uiGoogleTaskListAdapter.convert(it, map[it.listId])
      }

      _defTaskList.postValue(googleTaskLists.firstOrNull { it.isDefault() })
      _googleTaskLists.postValue(googleTaskLists)
      _allGoogleTasks.postValue(googleTasks)
    }
  }

  private fun mapTaskLists(list: List<GoogleTaskList>): Map<String, GoogleTaskList> {
    val map = mutableMapOf<String, GoogleTaskList>()
    list.forEach { map[it.listId] = it }
    return map
  }

  fun loadGoogleTasks() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    job = viewModelScope.launch(dispatcherProvider.default()) {
      var lists: TaskLists? = null
      try {
        lists = gTasks.taskLists()
      } catch (e: IOException) {
        e.printStackTrace()
      }

      if (lists != null && lists.size > 0 && lists.items != null) {
        for (item in lists.items) {
          val listId = item.id
          var taskList = googleTaskListsDao.getById(listId)
          if (taskList != null) {
            taskList.update(item)
          } else {
            val r = Random()
            val color = r.nextInt(15)
            taskList = GoogleTaskList(item, color)
          }
          Timber.d("loadGoogleTasks: $taskList")
          googleTaskListsDao.insert(taskList)
          val tasksList = gTasks.getTasks(listId)
          if (tasksList.isNotEmpty()) {
            for (task in tasksList) {
              var googleTask = googleTasksDao.getById(task.id)
              if (googleTask != null) {
                googleTask.update(task)
                googleTask.listId = task.id
              } else {
                googleTask = GoogleTask(task, listId)
              }
              googleTasksDao.insert(googleTask)
            }
          }
        }
        val local = googleTaskListsDao.all()
        val hasDefault = local.firstOrNull { it.isDefault() }
        if (hasDefault == null) {
          val listItem = local[0].apply {
            this.def = 1
            this.systemDefault = 1
          }
          googleTaskListsDao.insert(listItem)
        }
      }

      load()

      withUIContext {
        postInProgress(false)
      }
      job = null
    }
  }

  fun sync() {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    if (isSyncing) return
    isSyncing = true
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      var lists: TaskLists? = null
      try {
        lists = gTasks.taskLists()
      } catch (e: IOException) {
        e.printStackTrace()
      }

      if (lists != null && lists.size > 0 && lists.items != null) {
        for (item in lists.items) {
          val listId = item.id
          var taskList = googleTaskListsDao.getById(listId)
          if (taskList != null) {
            taskList.update(item)
          } else {
            val r = Random()
            val color = r.nextInt(15)
            taskList = GoogleTaskList(item, color)
          }
          googleTaskListsDao.insert(taskList)
          val tasks = gTasks.getTasks(listId)
          if (tasks.isEmpty()) {
            withUIContext {
              postInProgress(false)
              postCommand(Commands.UPDATED)
              updatesHelper.updateTasksWidget()
            }
          } else {
            val googleTasks = ArrayList<GoogleTask>()
            for (task in tasks) {
              var googleTask = googleTasksDao.getById(task.id)
              if (googleTask != null) {
                googleTask.listId = listId
                googleTask.update(task)
              } else {
                googleTask = GoogleTask(task, listId)
              }
              googleTasks.add(googleTask)
            }
            googleTasksDao.insertAll(googleTasks)
            withUIContext {
              postInProgress(false)
              postCommand(Commands.UPDATED)
              updatesHelper.updateTasksWidget()
            }
          }
        }

        val local = googleTaskListsDao.all()
        val hasDefault = local.firstOrNull { it.isDefault() }
        if (hasDefault == null) {
          val listItem = local[0].apply {
            this.def = 1
            this.systemDefault = 1
          }
          googleTaskListsDao.insert(listItem)
        }
      }

      load()

      isSyncing = false
    }
  }

  fun toggleTask(taskId: String) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTasksDao.getById(taskId)
        if (googleTask == null) {
          postInProgress(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        if (googleTask.status == GTasks.TASKS_NEED_ACTION) {
          gTasks.updateTaskStatus(GTasks.TASKS_COMPLETE, googleTask)
        } else {
          gTasks.updateTaskStatus(GTasks.TASKS_NEED_ACTION, googleTask)
        }
        load()
        postInProgress(false)
        postCommand(Commands.UPDATED)
        withUIContext {
          updatesHelper.updateTasksWidget()
        }
      } catch (e: IOException) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }
}
