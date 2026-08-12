package com.github.naz013.cloudapi.googletasks

import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList

interface GoogleTasksApi {
  fun initialize(): Boolean
  fun disconnect()
  suspend fun getTaskLists(): List<GoogleTaskList>
  suspend fun getTaskList(listId: String): GoogleTaskList?
  suspend fun saveTasksList(listTitle: String, color: Int): GoogleTaskList?
  suspend fun updateTasksList(newTitle: String, googleTaskList: GoogleTaskList): GoogleTaskList?
  suspend fun deleteTaskList(listId: String?): Boolean
  suspend fun clearTaskList(listId: String?): Boolean
  suspend fun saveTask(googleTask: GoogleTask): GoogleTask?
  suspend fun updateTaskStatus(status: String, googleTask: GoogleTask): GoogleTask?
  suspend fun updateTask(googleTask: GoogleTask): GoogleTask?
  suspend fun deleteTask(googleTask: GoogleTask): Boolean
  suspend fun getTasks(listId: String): List<GoogleTask>
  suspend fun moveTask(item: GoogleTask, oldList: String): GoogleTask?
}
