package com.github.naz013.sync.local

import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.RecurPresetRepository

internal class RecurPresetRepositoryCaller(
  private val recurPresetRepository: RecurPresetRepository
) : DataTypeRepositoryCaller<RecurPreset> {

  override suspend fun getById(id: String): RecurPreset? {
    return recurPresetRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return recurPresetRepository.getBySyncState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    recurPresetRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item is RecurPreset) {
      recurPresetRepository.save(item)
    }
  }

  override suspend fun getAllIds(): List<String> {
    return recurPresetRepository.getAllIds()
  }
}
