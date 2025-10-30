package com.github.naz013.sync.local

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.NoteRepository

internal class NoteRepositoryCaller(
  private val noteRepository: NoteRepository
) : DataTypeRepositoryCaller<NoteWithImages> {

  override suspend fun getById(id: String): NoteWithImages? {
    return noteRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return noteRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    noteRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is NoteWithImages) {
      throw IllegalArgumentException("Expected NoteWithImages but got ${item::class.java}")
    }
    item.note?.also { noteRepository.save(it.copy(syncState = SyncState.Synced)) }
    item.images.forEach { noteRepository.save(it) }
  }

  override suspend fun getAllIds(): List<String> {
    return noteRepository.getAllIds()
  }
}
