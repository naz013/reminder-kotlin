package com.github.naz013.repository

import com.github.naz013.domain.GoogleTaskList
import kotlinx.coroutines.flow.Flow

interface GoogleTaskListRepository {
  suspend fun save(googleTaskList: GoogleTaskList)

  suspend fun getById(id: String): GoogleTaskList?
  fun observeById(id: String): Flow<GoogleTaskList?>
  suspend fun getAll(): List<GoogleTaskList>
  fun observeAll(): Flow<List<GoogleTaskList>>
  suspend fun defaultGoogleTaskList(): GoogleTaskList?
  suspend fun getDefault(): List<GoogleTaskList>

  suspend fun delete(id: String)
  suspend fun deleteAll()
}
