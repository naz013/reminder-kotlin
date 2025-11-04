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

/**
 * Google Tasks API implementation for task and task list operations.
 *
 * Manages Google Tasks operations including creating, updating, deleting tasks and task lists.
 * Automatically initializes when the user is authorized and has Google Play Services.
 */
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

  /**
   * Retrieves all task lists from Google Tasks.
   *
   * @return List of GoogleTaskList, empty list if not initialized or on error
   */
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

  /**
   * Retrieves a specific task list by ID.
   *
   * @param listId The ID of the task list to retrieve
   * @return GoogleTaskList if found, null otherwise
   */
  override suspend fun getTaskList(listId: String): GoogleTaskList? {
    if (listId.isBlank() || !isInitialized) {
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

  /**
   * Saves a new task to Google Tasks.
   *
   * Creates a task in the specified list or the default list if not specified.
   *
   * @param googleTask The task to save
   * @return Updated GoogleTask with server data, or null if save fails
   */
  override suspend fun saveTask(googleTask: GoogleTask): GoogleTask? {
    if (!isInitialized || googleTask.title.isBlank()) {
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

  /**
   * Updates the status of a task.
   *
   * @param status The new status for the task
   * @param googleTask The task to update
   * @return Updated GoogleTask, or null if update fails
   */
  override suspend fun updateTaskStatus(status: String, googleTask: GoogleTask): GoogleTask? {
    if (!isInitialized || status.isBlank() || googleTask.listId.isBlank() || googleTask.taskId.isBlank()) {
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

  /**
   * Deletes a task from Google Tasks.
   *
   * @param googleTask The task to delete
   * @return true if deletion succeeded, false otherwise
   */
  override suspend fun deleteTask(googleTask: GoogleTask): Boolean {
    if (!isInitialized || googleTask.listId.isBlank() || googleTask.taskId.isBlank()) {
      return false
    }
    return try {
      withService { it.tasks().delete(googleTask.listId, googleTask.taskId).execute() }
      true
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to delete task id=${googleTask.taskId}", e)
      false
    }
  }

  /**
   * Updates an existing task in Google Tasks.
   *
   * @param googleTask The task to update with new data
   * @return Updated GoogleTask, or null if update fails
   */
  override suspend fun updateTask(googleTask: GoogleTask): GoogleTask? {
    if (!isInitialized || googleTask.listId.isBlank() || googleTask.taskId.isBlank()) {
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

  /**
   * Retrieves all tasks from a specific task list.
   *
   * @param listId The ID of the task list
   * @return List of GoogleTask, empty list if not initialized or on error
   */
  override suspend fun getTasks(listId: String): List<GoogleTask> {
    if (listId.isBlank() || !isInitialized) {
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

  /**
   * Creates a new task list.
   *
   * @param listTitle The title for the new task list
   * @param color The color code for the task list
   * @return Created GoogleTaskList, or null if creation fails
   */
  override suspend fun saveTasksList(listTitle: String, color: Int): GoogleTaskList? {
    if (!isInitialized || listTitle.isBlank()) {
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

  /**
   * Updates an existing task list's title.
   *
   * @param newTitle The new title for the task list
   * @param googleTaskList The task list to update
   * @return Updated GoogleTaskList, or null if update fails
   */
  override suspend fun updateTasksList(
    newTitle: String,
    googleTaskList: GoogleTaskList
  ): GoogleTaskList? {
    if (!isInitialized || newTitle.isBlank() || googleTaskList.listId.isBlank()) {
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

  /**
   * Deletes a task list from Google Tasks.
   *
   * @param listId The ID of the task list to delete
   * @return true if deletion succeeded, false otherwise
   */
  override suspend fun deleteTaskList(listId: String?): Boolean {
    if (!isInitialized || listId.isNullOrBlank()) {
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

  /**
   * Clears all tasks from a task list.
   *
   * @param listId The ID of the task list to clear
   * @return true if clearing succeeded, false otherwise
   */
  override suspend fun clearTaskList(listId: String?): Boolean {
    if (!isInitialized || listId.isNullOrBlank()) {
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

  /**
   * Moves a task from one list to another.
   *
   * Deletes the task from the old list and creates it in the new list.
   *
   * @param item The task to move (should contain the new list ID)
   * @param oldList The ID of the list to move from
   * @return Updated GoogleTask in the new list, or null if move fails
   */
  override suspend fun moveTask(item: GoogleTask, oldList: String): GoogleTask? {
    if (!isInitialized || oldList.isBlank() || item.taskId.isBlank()) {
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
