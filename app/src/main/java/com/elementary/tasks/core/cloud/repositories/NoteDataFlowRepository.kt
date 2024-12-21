package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository

class NoteDataFlowRepository(
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository
) : DatabaseRepository<NoteWithImages>() {
  override suspend fun get(id: String): NoteWithImages? {
    return noteRepository.getById(id)
  }

  override suspend fun insert(t: NoteWithImages) {
    val note = t.note
    if (note != null) {
      noteRepository.save(note)
      noteRepository.saveAll(t.images)
    }
  }

  override suspend fun all(): List<NoteWithImages> {
    return noteRepository.getAll()
  }

  override suspend fun delete(t: NoteWithImages) {
    val note = t.note ?: return
    noteRepository.delete(note.key)
    for (image in t.images) {
      noteRepository.deleteImage(image.id)
    }
    noteImageRepository.clearFolder(note.key)
  }
}
