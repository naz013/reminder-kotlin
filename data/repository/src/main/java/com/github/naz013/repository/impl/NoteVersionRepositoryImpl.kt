package com.github.naz013.repository.impl

import com.github.naz013.domain.note.NoteVersion
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteVersionRepository
import com.github.naz013.repository.dao.NoteVersionDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity

internal class NoteVersionRepositoryImpl(
  private val dao: NoteVersionDao
) : NoteVersionRepository {

  override suspend fun add(version: NoteVersion): Long {
    Logger.d(TAG, "Add version for note: ${version.noteKey}")
    return dao.insert(version.toEntity())
  }

  override suspend fun addAndPrune(version: NoteVersion, keep: Int): Long {
    Logger.d(TAG, "Add version for note: ${version.noteKey}, keep=$keep")
    return dao.insertAndPrune(version.toEntity(), keep)
  }

  override suspend fun getByNoteKey(noteKey: String): List<NoteVersion> {
    Logger.d(TAG, "Get versions for note: $noteKey")
    return dao.getByNoteKey(noteKey).map { it.toDomain() }
  }

  override suspend fun getById(id: Long): NoteVersion? {
    Logger.d(TAG, "Get version by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun deleteByNoteKey(noteKey: String) {
    Logger.d(TAG, "Delete versions for note: $noteKey")
    dao.deleteByNoteKey(noteKey)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all note versions")
    dao.deleteAll()
  }

  override suspend fun prune(noteKey: String, keep: Int) {
    Logger.d(TAG, "Prune versions for note: $noteKey, keep=$keep")
    dao.prune(noteKey, keep)
  }

  companion object {
    private const val TAG = "NoteVersionRepository"
  }
}
