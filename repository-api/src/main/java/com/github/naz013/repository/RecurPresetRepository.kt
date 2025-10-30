package com.github.naz013.repository

import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.sync.SyncState

interface RecurPresetRepository {
  suspend fun save(recurPreset: RecurPreset)

  suspend fun getById(id: String): RecurPreset?
  suspend fun getAll(): List<RecurPreset>
  suspend fun getAllIds(): List<String>
  suspend fun getAllByType(presetType: PresetType? = null): List<RecurPreset>
  suspend fun getBySyncState(states: List<SyncState>): List<String>

  suspend fun delete(id: String)
  suspend fun deleteAll()

  suspend fun updateSyncState(id: String, state: SyncState)
}
