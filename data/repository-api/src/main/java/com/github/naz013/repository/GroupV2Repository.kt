package com.github.naz013.repository

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.flow.Flow

interface GroupV2Repository {
  suspend fun save(group: GroupV2)
  suspend fun saveAll(groups: List<GroupV2>)

  suspend fun getAll(): List<GroupV2>
  fun observeAll(): Flow<List<GroupV2>>
  suspend fun getById(id: String): GroupV2?
  suspend fun defaultGroup(isDef: Boolean = true): GroupV2?
  suspend fun search(query: String): List<GroupV2>

  suspend fun delete(id: String)
  suspend fun deleteAll()

  suspend fun updateSyncState(id: String, state: SyncState)
  suspend fun getIdsByState(syncStates: List<SyncState>): List<String>
  suspend fun getAllIds(): List<String>
  suspend fun setDefaultGroup(id: String, isDef: Boolean = false)

  suspend fun countAll(): Int
}
