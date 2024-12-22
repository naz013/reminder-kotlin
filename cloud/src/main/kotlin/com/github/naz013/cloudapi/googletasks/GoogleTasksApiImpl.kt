package com.github.naz013.cloudapi.googletasks

import android.content.Context
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.logging.Logger
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.tasks.Tasks
import com.google.api.services.tasks.TasksScopes
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import java.util.Collections

internal class GoogleTasksApiImpl(
  private val context: Context,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val googleTasksModelFactory: GoogleTasksModelFactory
) : GoogleTasksApi {

  private var tasks: Tasks? = null
  private var isInitialized: Boolean = false

  init {
    initialize()
  }

  override fun initialize(): Boolean {
    if (googleTasksAuthManager.hasGooglePlayServices() && googleTasksAuthManager.isAuthorized()) {
      val credential = GoogleAccountCredential.usingOAuth2(
        context,
        Collections.singleton(TasksScopes.TASKS)
      )
      credential.selectedAccountName = googleTasksAuthManager.getUserName()
      tasks = Tasks.Builder(NetHttpTransport(), GsonFactory(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build()
      isInitialized = true
      Logger.i(TAG, "Google Tasks initialized")
    } else {
      tasks = null
      isInitialized = false
      Logger.i(TAG, "Google Tasks not initialized")
    }
    return isInitialized
  }

  override fun disconnect() {
    tasks = null
    isInitialized = false
  }

  override suspend fun getTaskLists(): List<GoogleTaskList> {
    if (!isInitialized) {
      return emptyList()
    }
    return try {
      withService { it.tasklists().list().execute() }?.items
        ?.filterNotNull()
        ?.map { googleTasksModelFactory.toDomain(it) }
        ?: emptyList()
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to get task lists", e)
      emptyList()
    }
  }

  override suspend fun getTaskList(listId: String): GoogleTaskList? {
    if (!isInitialized) {
      return null
    }
    return try {
      withService { it.tasklists().get(listId).execute() }
        ?.let { googleTasksModelFactory.toDomain(it) }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to get task list id=$listId", e)
      null
    }
  }

  override suspend fun saveTask(googleTask: GoogleTask): GoogleTask? {
    if (!isInitialized) {
      return null
    }
    if (googleTask.title.isEmpty()) {
      return null
    }
    try {
      val task = googleTasksModelFactory.toModel(googleTask)
      val result: Task?
      val listId = googleTask.listId
      if (listId.isNotEmpty()) {
        result = withService { it.tasks().insert(listId, task).execute() }
      } else {
        result = withService { it.tasks().insert("@default", task).execute() }
        val list = withService { it.tasklists().get("@default").execute() }
        if (list != null) {
          googleTask.listId = list.id
        }
      }
      if (result != null) {
        return googleTasksModelFactory.update(googleTask, result)
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to insert task id=${googleTask.taskId}", e)
      return null
    }
    return null
  }

  override suspend fun updateTaskStatus(status: String, googleTask: GoogleTask): GoogleTask? {
    if (!isInitialized) {
      return null
    }
    try {
      val task = withService { it.tasks().get(googleTask.listId, googleTask.taskId).execute() }
        ?: return null
      task.status = status
      if (status == GoogleTask.TASKS_NEED_ACTION) {
        task.completed = null
      }
      task.updated = googleTasksModelFactory.toRfc3339Format(System.currentTimeMillis())
      val result = withService { it.tasks().update(googleTask.listId, task.id, task).execute() }
      if (result != null) {
        return googleTasksModelFactory.update(googleTask, result)
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to update task status id=${googleTask.taskId}", e)
      return null
    }
    return null
  }

  override suspend fun deleteTask(googleTask: GoogleTask): Boolean {
    if (!isInitialized) {
      return false
    }
    if (googleTask.listId.isEmpty()) return false
    return try {
      withService { it.tasks().delete(googleTask.listId, googleTask.taskId).execute() }
      true
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to delete task id=${googleTask.taskId}", e)
      false
    }
  }

  override suspend fun updateTask(googleTask: GoogleTask): GoogleTask? {
    if (!isInitialized) {
      return null
    }
    try {
      val task = withService { it.tasks().get(googleTask.listId, googleTask.taskId).execute() }
        ?: return null
      task.status = GoogleTask.TASKS_NEED_ACTION
      task.title = googleTask.title
      task.completed = null
      if (googleTask.dueDate != 0L) {
        task.due = googleTasksModelFactory.toRfc3339Format(googleTask.dueDate)
      }
      if (googleTask.notes.isNotEmpty()) {
        task.notes = googleTask.notes
      }
      task.updated = googleTasksModelFactory.toRfc3339Format(System.currentTimeMillis())
      val result = withService { it.tasks().update(googleTask.listId, task.id, task).execute() }
      if (result != null) {
        return googleTasksModelFactory.update(googleTask, result)
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to update task id=${googleTask.taskId}", e)
    }
    return null
  }

  override suspend fun getTasks(listId: String): List<GoogleTask> {
    if (!isInitialized) {
      return emptyList()
    }
    try {
      return withService { it.tasks().list(listId).execute().items }
        ?.map { googleTasksModelFactory.toDomain(it, listId) }
        ?: emptyList()
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to get tasks listId=$listId", e)
    }
    return emptyList()
  }

  override suspend fun saveTasksList(listTitle: String, color: Int): GoogleTaskList? {
    if (!isInitialized) {
      return null
    }
    val taskList = TaskList()
    taskList.title = listTitle
    return try {
      val result = withService { it.tasklists().insert(taskList).execute() } ?: return null
      googleTasksModelFactory.toDomain(result, color)
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to insert task list $listTitle", e)
      null
    }
  }

  override suspend fun updateTasksList(
    newTitle: String,
    googleTaskList: GoogleTaskList
  ): GoogleTaskList? {
    if (!isInitialized) {
      return null
    }
    val listId = googleTaskList.listId
    try {
      val taskList = withService { it.tasklists().get(listId).execute() } ?: return null
      taskList.title = newTitle
      val result = withService { it.tasklists().update(listId, taskList).execute() }
      if (result != null) {
        return googleTasksModelFactory.update(googleTaskList, result)
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to update task list $newTitle", e)
    }
    return null
  }

  override suspend fun deleteTaskList(listId: String?): Boolean {
    if (!isInitialized) {
      return false
    }
    if (listId == null) {
      return false
    }
    return try {
      withService { it.tasklists().delete(listId).execute() }
      true
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to delete task list", e)
      false
    }
  }

  override suspend fun clearTaskList(listId: String?): Boolean {
    if (!isInitialized) {
      return false
    }
    if (listId == null) {
      return false
    }
    return try {
      withService { it.tasks().clear(listId).execute() }
      true
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to clear task list", e)
      false
    }
  }

  override suspend fun moveTask(item: GoogleTask, oldList: String): GoogleTask? {
    if (!isInitialized) {
      return null
    }
    try {
      val task = withService { it.tasks().get(oldList, item.taskId).execute() }
      if (task != null) {
        val clone = GoogleTask(item)
        clone.listId = oldList
        deleteTask(clone)
        return saveTask(item)
      }
    } catch (e: Exception) {
      Logger.e(e, "Failed to move task")
    }
    return null
  }

  private fun <T> withService(call: (Tasks) -> T): T? {
    val service = tasks?.takeIf { isInitialized } ?: return null
    return call.invoke(service)
  }

  companion object {
    private const val TAG = "GoogleTasksApi"
    private const val APPLICATION_NAME = "Reminder/7.0"
  }
}
