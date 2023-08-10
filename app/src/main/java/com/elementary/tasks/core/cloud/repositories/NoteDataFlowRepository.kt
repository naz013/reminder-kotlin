package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.data.repository.NoteRepository

class NoteDataFlowRepository(
  appDb: AppDb,
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository
) : DatabaseRepository<NoteWithImages>(appDb) {
  override suspend fun get(id: String): NoteWithImages? {
    return appDb.notesDao().getById(id)
  }

  override suspend fun insert(t: NoteWithImages) {
    val note = t.note
    if (note != null) {
      appDb.notesDao().insert(note)
      appDb.notesDao().insertAll(t.images)
    }
  }

  override suspend fun all(): List<NoteWithImages> {
    return noteRepository.getAll()
  }

  override suspend fun delete(t: NoteWithImages) {
    val note = t.note ?: return
    appDb.notesDao().delete(note)
    for (image in t.images) {
      appDb.notesDao().delete(image)
    }
    noteImageRepository.clearFolder(note.key)
  }
}
