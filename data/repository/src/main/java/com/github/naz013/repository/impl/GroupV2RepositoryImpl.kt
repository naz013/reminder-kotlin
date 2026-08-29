package com.github.naz013.repository.impl

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.dao.GroupV2Dao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GroupV2RepositoryImpl(
  private val dao: GroupV2Dao,
  private val tableChangeNotifier: TableChangeNotifier
) : GroupV2Repository {

  private val table = Table.GroupV2

  override suspend fun save(group: GroupV2) {
    Logger.d(TAG, "Save group: ${group.uuId}")
    dao.insert(group.toEntity())
    tableChangeNotifier.notify(table)
  }

  override suspend fun saveAll(groups: List<GroupV2>) {
    Logger.d(TAG, "Save all groups, size = ${groups.size}")
    dao.insertAll(groups.map { it.toEntity() })
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAll(): List<GroupV2> {
    Logger.d(TAG, "Get all groups")
    return dao.all().map { it.toDomain() }
  }

  override fun observeAll(): Flow<List<GroupV2>> =
    dao.observeAll().map { list -> list.map { it.toDomain() } }

  override suspend fun getById(id: String): GroupV2? {
    Logger.d(TAG, "Get group by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun defaultGroup(isDef: Boolean): GroupV2? {
    Logger.d(TAG, "Get default group")
    return dao.defaultGroup(isDef)?.toDomain()
  }

  override suspend fun search(query: String): List<GroupV2> {
    Logger.d(TAG, "Search groups by query: $query")
    return dao.search(query).map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete group by id: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all groups")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Update group sync state: $id to $state")
    dao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(table)
  }

  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> {
    Logger.d(TAG, "Get group ids by sync states: $syncStates")
    return dao.getBySyncStates(syncStates.map { it.name })
  }

  override suspend fun getAllIds(): List<String> {
    Logger.d(TAG, "Get all group ids")
    return dao.getAllIds()
  }

  override suspend fun setDefaultGroup(id: String, isDef: Boolean) {
    Logger.d(TAG, "Set group as default: $id")
    dao.setDefaultGroup(id, isDef)
    tableChangeNotifier.notify(table)
  }

  override suspend fun countAll(): Int {
    Logger.d(TAG, "Count all groups")
    return dao.countAll()
  }

  companion object {
    private const val TAG = "GroupV2Repository"
  }
}
