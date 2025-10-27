package com.github.naz013.repository.impl

import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.dao.ReminderGroupDao
import com.github.naz013.repository.entity.ReminderGroupEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class ReminderGroupRepositoryImpl(
  private val dao: ReminderGroupDao,
  private val tableChangeNotifier: TableChangeNotifier
) : ReminderGroupRepository {

  private val table = Table.ReminderGroup

  override suspend fun save(reminderGroup: ReminderGroup) {
    Logger.d(TAG, "Save reminder group: ${reminderGroup.groupUuId}")
    dao.insert(ReminderGroupEntity(reminderGroup))
    tableChangeNotifier.notify(table)
  }

  override suspend fun saveAll(reminderGroups: List<ReminderGroup>) {
    Logger.d(TAG, "Save all reminder groups, size = ${reminderGroups.size}")
    dao.insertAll(reminderGroups.map { ReminderGroupEntity(it) })
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAll(): List<ReminderGroup> {
    Logger.d(TAG, "Get all reminder groups")
    return dao.all().map { it.toDomain() }
  }

  override suspend fun getById(id: String): ReminderGroup? {
    Logger.d(TAG, "Get reminder group by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun defaultGroup(isDef: Boolean): ReminderGroup? {
    Logger.d(TAG, "Get default group")
    return dao.defaultGroup(isDef)?.toDomain()
  }

  override suspend fun search(query: String): List<ReminderGroup> {
    Logger.d(TAG, "Search reminder groups by query: $query")
    return dao.search(query).map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete reminder group by id: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all reminder groups")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> {
    Logger.d(TAG, "Get reminder group ids by sync states: $syncStates")
    return dao.getBySyncStates(syncStates.map { it.name })
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Update reminder group sync state: $id to $state")
    dao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "ReminderGroupRepository"
  }
}
