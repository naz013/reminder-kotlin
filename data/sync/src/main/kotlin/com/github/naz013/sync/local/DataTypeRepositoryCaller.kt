package com.github.naz013.sync.local

import com.github.naz013.domain.sync.SyncState

internal interface DataTypeRepositoryCaller<T> {
  suspend fun getById(id: String): T?
  suspend fun getIdsByState(states: List<SyncState>): List<String>
  suspend fun updateSyncState(id: String, state: SyncState)
  suspend fun insertOrUpdate(item: Any)
  suspend fun getAllIds(): List<String>
}
