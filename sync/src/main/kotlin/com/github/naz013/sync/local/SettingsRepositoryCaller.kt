package com.github.naz013.sync.local

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.sync.settings.SettingsModel

internal class SettingsRepositoryCaller : DataTypeRepositoryCaller<SettingsModel> {

  override suspend fun getById(id: String): SettingsModel? {
    return null
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return emptyList()
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
  }

  override suspend fun insertOrUpdate(item: Any) {
  }

  override suspend fun getAllIds(): List<String> {
    return emptyList()
  }
}
