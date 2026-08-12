package com.github.naz013.repository

import com.github.naz013.domain.GoogleTaskList

interface GoogleTaskListRepository {
  suspend fun save(googleTaskList: GoogleTaskList)

  suspend fun getById(id: String): GoogleTaskList?
  suspend fun getAll(): List<GoogleTaskList>
  suspend fun defaultGoogleTaskList(): GoogleTaskList?
  suspend fun getDefault(): List<GoogleTaskList>

  suspend fun delete(id: String)
  suspend fun deleteAll()
}
