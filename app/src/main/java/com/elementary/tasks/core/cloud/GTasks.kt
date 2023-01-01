package com.elementary.tasks.core.cloud

import android.content.Context
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.Data
import com.google.api.client.util.DateTime
import com.google.api.services.tasks.Tasks
import com.google.api.services.tasks.TasksScopes
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import com.google.api.services.tasks.model.TaskLists
import timber.log.Timber
import java.util.Collections

class GTasks(
  private val context: Context,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val prefs: Prefs
) {

  private var tasksService: Tasks? = null

  var statusCallback: StatusCallback? = null
  var isLogged: Boolean = false
    get() {
      Timber.d("isLogged: $field")
      return field
    }
    private set

  init {
    val user = prefs.tasksUser
    login(user)
  }

  fun login(user: String) {
    Timber.d("login: ")
    if (SuperUtil.isGooglePlayServicesAvailable(context) && user.matches(".*@.*".toRegex())) {
      Timber.d("user -> $user")
      val credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(TasksScopes.TASKS))
      credential.selectedAccountName = user
      tasksService = Tasks.Builder(NetHttpTransport(), GsonFactory(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build()
      isLogged = true
      statusCallback?.onStatusChanged(true)
    } else {
      logOut()
    }
  }

  fun logOut() {
    Timber.d("logOut: ")
    prefs.tasksUser = Prefs.DRIVE_USER_NONE
    tasksService = null
    isLogged = false
    statusCallback?.onStatusChanged(false)
  }

  fun taskLists(): TaskLists? {
    return try {
      withService { it.tasklists().list().execute() }
    } catch (e: Exception) {
      Timber.e(e, "Failed to get task lists")
      null
    }
  }

  fun insertTask(item: GoogleTask): Boolean {
    if (item.title.isEmpty()) {
      return false
    }
    try {
      val task = Task()
      task.title = item.title
      if (item.notes.isNotEmpty()) {
        task.notes = item.notes
      }
      if (item.dueDate != 0L) {
        task.due = DateTime(item.dueDate)
      }
      val result: Task?
      val listId = item.listId
      if (listId.isNotEmpty()) {
        result = withService { it.tasks().insert(listId, task).execute() }
      } else {
        val googleTaskList = googleTaskListsDao.defaultGoogleTaskList()
        if (googleTaskList != null) {
          item.listId = googleTaskList.listId
          result = withService { it.tasks().insert(googleTaskList.listId, task).execute() }
        } else {
          result = withService { it.tasks().insert("@default", task).execute() }
          val list = withService { it.tasklists().get("@default").execute() }
          if (list != null) {
            item.listId = list.id
          }
        }
      }
      if (result != null) {
        item.update(result)
        googleTasksDao.insert(item)
        return true
      }
    } catch (e: Exception) {
      Timber.e(e, "Failed to insert task id=${item.taskId}")
      return false
    }
    return false
  }

  fun updateTaskStatus(status: String, item: GoogleTask) {
    try {
      val task = withService { it.tasks().get(item.listId, item.taskId).execute() } ?: return
      task.status = status
      if (status == TASKS_NEED_ACTION) {
        task.completed = Data.NULL_DATE_TIME
      }
      task.updated = DateTime(System.currentTimeMillis())
      val result = withService { it.tasks().update(item.listId, task.id, task).execute() }
      if (result != null) {
        item.update(result)
        googleTasksDao.insert(item)
      }
    } catch (e: Exception) {
      Timber.e(e, "Failed to update task status id=${item.taskId}")
    }
  }

  fun deleteTask(item: GoogleTask) {
    if (item.listId.isEmpty()) return
    try {
      withService { it.tasks().delete(item.listId, item.taskId).execute() }
    } catch (e: Exception) {
      Timber.e(e, "Failed to delete task id=${item.taskId}")
    }
  }

  fun updateTask(item: GoogleTask) {
    try {
      val task = withService { it.tasks().get(item.listId, item.taskId).execute() } ?: return
      task.status = TASKS_NEED_ACTION
      task.title = item.title
      task.completed = Data.NULL_DATE_TIME
      if (item.dueDate != 0L) task.due = DateTime(item.dueDate)
      if (item.notes != "") task.notes = item.notes
      task.updated = DateTime(System.currentTimeMillis())
      withService { it.tasks().update(item.listId, task.id, task).execute() }
    } catch (e: Exception) {
      Timber.e(e, "Failed to update task id=${item.taskId}")
    }
  }

  fun getTasks(listId: String): List<Task> {
    try {
      return withService { it.tasks().list(listId).execute().items } ?: emptyList()
    } catch (e: Exception) {
      Timber.e(e, "Failed to get tasks listId=$listId")
    }
    return emptyList()
  }

  fun insertTasksList(listTitle: String, color: Int) {
    val taskList = TaskList()
    taskList.title = listTitle
    try {
      val result = withService { it.tasklists().insert(taskList).execute() } ?: return
      val item = GoogleTaskList(result, color)
      googleTaskListsDao.insert(item)
    } catch (e: Exception) {
      Timber.e(e, "Failed to insert task list $listTitle")
    }
  }

  fun updateTasksList(listTitle: String, listId: String?) {
    if (listId == null) {
      return
    }
    try {
      val taskList = withService { it.tasklists().get(listId).execute() } ?: return
      taskList.title = listTitle
      withService { it.tasklists().update(listId, taskList).execute() }
      val item = googleTaskListsDao.getById(listId)
      if (item != null) {
        item.update(taskList)
        googleTaskListsDao.insert(item)
      }
    } catch (e: Exception) {
      Timber.e(e, "Failed to update task list $listTitle")
    }
  }

  fun deleteTaskList(listId: String?) {
    if (listId == null) {
      return
    }
    try {
      withService { it.tasklists().delete(listId).execute() }
    } catch (e: Exception) {
      Timber.e(e, "Failed to delete task list")
    }
  }

  fun clearTaskList(listId: String?) {
    if (listId == null) {
      return
    }
    try {
      withService { it.tasks().clear(listId).execute() }
    } catch (e: Exception) {
      Timber.e(e, "Failed to clear task list")
    }
  }

  fun moveTask(item: GoogleTask, oldList: String): Boolean {
    try {
      val task = withService { it.tasks().get(oldList, item.taskId).execute() }
      if (task != null) {
        val clone = GoogleTask(item)
        clone.listId = oldList
        deleteTask(clone)
        googleTasksDao.delete(item)
        return insertTask(item)
      }
    } catch (e: Exception) {
      Timber.e(e, "Failed to move task")
    }
    return false
  }

  private fun <T> withService(call: (Tasks) -> T): T? {
    val service = tasksService?.takeIf { isLogged } ?: return null
    return call.invoke(service)
  }

  interface StatusCallback {
    fun onStatusChanged(isLogged: Boolean)
  }

  companion object {
    const val TASKS_NEED_ACTION = "needsAction"
    const val TASKS_COMPLETE = "completed"
    private const val APPLICATION_NAME = "Reminder/7.0"
  }
}
