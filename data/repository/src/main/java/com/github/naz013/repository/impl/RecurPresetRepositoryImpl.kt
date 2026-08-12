package com.github.naz013.repository.impl

import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.dao.RecurPresetDao
import com.github.naz013.repository.entity.RecurPresetEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class RecurPresetRepositoryImpl(
  private val dao: RecurPresetDao,
  private val notifier: TableChangeNotifier
) : RecurPresetRepository {

  private val table = Table.RecurPreset

  override suspend fun save(recurPreset: RecurPreset) {
    Logger.d(TAG, "Save recur preset ${recurPreset.id}")
    dao.insert(RecurPresetEntity(recurPreset))
    notifier.notify(table)
  }

  override suspend fun getById(id: String): RecurPreset? {
    Logger.d(TAG, "Get recur preset by id $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun getAll(): List<RecurPreset> {
    Logger.d(TAG, "Get all recur presets")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getAllByType(presetType: PresetType?): List<RecurPreset> {
    Logger.d(TAG, "Get all recur presets by type $presetType")
    return if (presetType == null) {
      dao.getAll().map { it.toDomain() }
    } else {
      dao.getAllByType(presetType.ordinal).map { it.toDomain() }
    }
  }

  override suspend fun getBySyncState(states: List<SyncState>): List<String> {
    Logger.d(TAG, "Get recur presets by sync states $states")
    return dao.getBySyncStates(states.map { it.name })
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete recur preset by id $id")
    dao.deleteById(id)
    notifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all recur presets")
    dao.deleteAll()
    notifier.notify(table)
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Update recur preset sync state for id $id to $state")
    dao.updateSyncState(id, state.name)
    notifier.notify(table)
  }

  override suspend fun getAllIds(): List<String> {
    Logger.d(TAG, "Get all recur preset ids")
    return dao.getAllIds()
  }

  companion object {
    private const val TAG = "RecurPresetRepository"
  }
}
