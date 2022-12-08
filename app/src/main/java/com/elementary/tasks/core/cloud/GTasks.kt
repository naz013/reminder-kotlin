package com.elementary.tasks.core.cloud

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.SuperUtil
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
import java.util.*

class GTasks(
  private val context: Context,
  private val appDb: AppDb,
  private val prefs: Prefs
) {

  private var tasksService: Tasks? = null

  var statusObserver: ((Boolean) -> Unit)? = null
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
      Timber.d("GTasks: user -> $user")
      val credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(TasksScopes.TASKS))
      credential.selectedAccountName = user
      tasksService = Tasks.Builder(NetHttpTransport(), GsonFactory(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build()
      isLogged = true
      statusObserver?.invoke(true)
    } else {
      logOut()
    }
  }

  fun logOut() {
    Timber.d("logOut: ")
    prefs.tasksUser = Prefs.DRIVE_USER_NONE
    tasksService = null
    isLogged = false
    statusObserver?.invoke(false)
  }

  fun taskLists(): TaskLists? {
    return try {
      if (!isLogged || tasksService == null) null else tasksService?.tasklists()?.list()?.execute()
    } catch (e: Exception) {
      null
    }
  }

  fun insertTask(item: GoogleTask): Boolean {
    if (!isLogged || TextUtils.isEmpty(item.title) || tasksService == null) {
      return false
    }
    try {
      val task = Task()
      task.title = item.title
      if (item.notes != "") {
        task.notes = item.notes
      }
      if (item.dueDate != 0L) {
        task.due = DateTime(item.dueDate)
      }
      val result: Task?
      val listId = item.listId
      if (!TextUtils.isEmpty(listId)) {
        result = tasksService?.tasks()?.insert(listId, task)?.execute()
      } else {
        val googleTaskList = appDb.googleTaskListsDao().defaultGoogleTaskList()
        if (googleTaskList != null) {
          item.listId = googleTaskList.listId
          result = tasksService?.tasks()?.insert(googleTaskList.listId, task)?.execute()
        } else {
          result = tasksService?.tasks()?.insert("@default", task)?.execute()
          val list = tasksService?.tasklists()?.get("@default")?.execute()
          if (list != null) {
            item.listId = list.id
          }
        }
      }
      if (result != null) {
        item.update(result)
        appDb.googleTasksDao().insert(item)
        return true
      }
    } catch (e: Exception) {
      return false
    }
    return false
  }

  fun updateTaskStatus(status: String, googleTask: GoogleTask) {
    if (!isLogged || tasksService == null) return
    try {
      val task = tasksService?.tasks()?.get(googleTask.listId, googleTask.taskId)?.execute()
        ?: return
      task.status = status
      if (status == TASKS_NEED_ACTION) {
        task.completed = Data.NULL_DATE_TIME
      }
      task.updated = DateTime(System.currentTimeMillis())
      val result = tasksService?.tasks()?.update(googleTask.listId, task.id, task)?.execute()
      if (result != null) {
        googleTask.update(result)
        appDb.googleTasksDao().insert(googleTask)
      }
    } catch (e: Exception) {
    }
  }

  fun deleteTask(item: GoogleTask) {
    if (!isLogged || item.listId == "" || tasksService == null) return
    try {
      tasksService?.tasks()?.delete(item.listId, item.taskId)?.execute()
    } catch (e: Exception) {
    }
  }

  fun updateTask(item: GoogleTask) {
    if (!isLogged || tasksService == null) return
    try {
      val task = tasksService?.tasks()?.get(item.listId, item.taskId)?.execute() ?: return
      task.status = TASKS_NEED_ACTION
      task.title = item.title
      task.completed = Data.NULL_DATE_TIME
      if (item.dueDate != 0L) task.due = DateTime(item.dueDate)
      if (item.notes != "") task.notes = item.notes
      task.updated = DateTime(System.currentTimeMillis())
      tasksService?.tasks()?.update(item.listId, task.id, task)?.execute()
    } catch (e: Exception) {
    }
  }

  fun getTasks(listId: String): List<Task> {
    var taskLists: List<Task> = ArrayList()
    if (!isLogged || tasksService == null) return taskLists
    try {
      taskLists = tasksService?.tasks()?.list(listId)?.execute()?.items ?: arrayListOf()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return taskLists
  }

  fun insertTasksList(listTitle: String, color: Int) {
    if (!isLogged || tasksService == null) return
    val taskList = TaskList()
    taskList.title = listTitle
    try {
      val result = tasksService?.tasklists()?.insert(taskList)?.execute() ?: return
      val item = GoogleTaskList(result, color)
      appDb.googleTaskListsDao().insert(item)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun updateTasksList(listTitle: String, listId: String?) {
    if (!isLogged || listId == null || tasksService == null) {
      return
    }
    try {
      val taskList = tasksService?.tasklists()?.get(listId)?.execute() ?: return
      taskList.title = listTitle
      tasksService?.tasklists()?.update(listId, taskList)?.execute()
      val item = appDb.googleTaskListsDao().getById(listId)
      if (item != null) {
        item.update(taskList)
        appDb.googleTaskListsDao().insert(item)
      }
    } catch (e: Exception) {
    }
  }

  fun deleteTaskList(listId: String?) {
    if (!isLogged || listId == null || tasksService == null) {
      return
    }
    try {
      tasksService?.tasklists()?.delete(listId)?.execute()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun clearTaskList(listId: String?) {
    if (!isLogged || listId == null || tasksService == null) {
      return
    }
    try {
      tasksService?.tasks()?.clear(listId)?.execute()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun moveTask(item: GoogleTask, oldList: String): Boolean {
    if (!isLogged || tasksService == null) {
      return false
    }
    try {
      val task = tasksService?.tasks()?.get(oldList, item.taskId)?.execute()
      if (task != null) {
        val clone = GoogleTask(item)
        clone.listId = oldList
        deleteTask(clone)
        appDb.googleTasksDao().delete(item)
        return insertTask(item)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }

  companion object {
    const val TASKS_NEED_ACTION = "needsAction"
    const val TASKS_COMPLETE = "completed"
    private const val APPLICATION_NAME = "Reminder/7.0"
  }
}
