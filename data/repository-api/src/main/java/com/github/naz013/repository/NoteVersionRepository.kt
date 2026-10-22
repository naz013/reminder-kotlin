package com.github.naz013.repository

import com.github.naz013.domain.note.NoteVersion

interface NoteVersionRepository {
  suspend fun add(version: NoteVersion): Long

  /** Newest first. */
  suspend fun getByNoteKey(noteKey: String): List<NoteVersion>

  suspend fun getById(id: Long): NoteVersion?

  suspend fun deleteByNoteKey(noteKey: String)

  suspend fun deleteAll()

  /** Deletes all but the [keep] newest versions of the note. */
  suspend fun prune(noteKey: String, keep: Int)
}
