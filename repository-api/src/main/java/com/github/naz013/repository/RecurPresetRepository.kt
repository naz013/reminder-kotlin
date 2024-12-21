package com.github.naz013.repository

import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset

interface RecurPresetRepository {
  suspend fun save(recurPreset: RecurPreset)

  suspend fun getById(id: String): RecurPreset?
  suspend fun getAll(): List<RecurPreset>
  suspend fun getAllByType(presetType: PresetType? = null): List<RecurPreset>

  suspend fun delete(id: String)
  suspend fun deleteAll()
}
