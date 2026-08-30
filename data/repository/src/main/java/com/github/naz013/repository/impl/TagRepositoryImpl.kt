package com.github.naz013.repository.impl

import com.github.naz013.domain.Tag
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.TagRepository
import com.github.naz013.repository.TagSyncTrigger
import com.github.naz013.repository.dao.TagDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TagRepositoryImpl(
  private val dao: TagDao,
  private val tableChangeNotifier: TableChangeNotifier,
  private val tagSyncTrigger: TagSyncTrigger
) : TagRepository {

  private val table = Table.Tag

  override fun observeAll(): Flow<List<Tag>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

  override suspend fun getAll(): List<Tag> {
    Logger.d(TAG, "Get all tags")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getById(id: String): Tag? {
    Logger.d(TAG, "Get tag by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override fun observeById(id: String): Flow<Tag?> =
    dao.observeById(id).map { it?.toDomain() }

  override suspend fun save(tag: Tag) {
    Logger.d(TAG, "Save tag: ${tag.id}")
    dao.insert(tag.toEntity())
    tableChangeNotifier.notify(table)
    tagSyncTrigger.onTagSaved(tag.id)
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete tag: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
    tagSyncTrigger.onTagDeleted(id)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all tags")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    Logger.d(TAG, "Get tags by sync states: $states")
    return dao.getBySyncStates(states.map { it.name })
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Updating sync state for tag id: $id to state: $state")
    dao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAllIds(): List<String> {
    Logger.d(TAG, "Get all tag ids")
    return dao.getAllIds()
  }

  companion object {
    private const val TAG = "TagRepository"
  }
}
