package com.github.naz013.repository

import com.github.naz013.domain.GoogleTask

interface GoogleTaskRepository {
  suspend fun save(googleTask: GoogleTask)
  suspend fun saveAll(googleTasks: List<GoogleTask>)

  suspend fun getById(id: String): GoogleTask?
  suspend fun getByReminderId(id: String): GoogleTask?
  suspend fun getAll(): List<GoogleTask>
  suspend fun search(query: String): List<GoogleTask>
  suspend fun getAllByList(listId: String, status: String): List<GoogleTask>
  suspend fun getAllByList(listId: String): List<GoogleTask>
  suspend fun getAttachedToReminder(): List<GoogleTask>

  suspend fun delete(id: String)
  suspend fun deleteAll()
  suspend fun deleteAll(ids: List<String>)
  suspend fun deleteAll(listId: String)
}
