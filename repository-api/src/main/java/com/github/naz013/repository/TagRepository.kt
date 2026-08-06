package com.github.naz013.repository

import com.github.naz013.domain.Tag
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.flow.Flow

interface TagRepository {
  fun observeAll(): Flow<List<Tag>>

  suspend fun getAll(): List<Tag>

  suspend fun getById(id: String): Tag?

  suspend fun save(tag: Tag)

  suspend fun delete(id: String)

  suspend fun deleteAll()

  suspend fun getIdsByState(states: List<SyncState>): List<String>

  suspend fun updateSyncState(id: String, state: SyncState)

  suspend fun getAllIds(): List<String>
}
