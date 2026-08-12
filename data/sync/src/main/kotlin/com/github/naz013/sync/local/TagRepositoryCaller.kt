package com.github.naz013.sync.local

import com.github.naz013.domain.Tag
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.TagRepository

internal class TagRepositoryCaller(
  private val tagRepository: TagRepository
) : DataTypeRepositoryCaller<Tag> {

  override suspend fun getById(id: String): Tag? {
    return tagRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return tagRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    tagRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is Tag) {
      throw IllegalArgumentException("Invalid item type: ${item::class.java}, expected: ${Tag::class.java}")
    }
    tagRepository.save(item.copy(syncState = SyncState.Synced))
  }

  override suspend fun getAllIds(): List<String> {
    return tagRepository.getAllIds()
  }
}
