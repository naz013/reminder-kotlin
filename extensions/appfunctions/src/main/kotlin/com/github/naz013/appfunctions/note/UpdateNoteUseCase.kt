package com.github.naz013.appfunctions.note

import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.NoteRepository

class UpdateNoteUseCase(
  private val noteRepository: NoteRepository,
) {
  suspend operator fun invoke(
    id: String,
    title: String,
    content: String,
  ): Note? {
    val existing = noteRepository.getById(id)?.note ?: return null
    val updated =
      existing.copy(
        content = NoteDocument.fromLegacy(title = title, summary = content),
        version = existing.version + 1,
      )
    noteRepository.save(updated)
    noteRepository.updateSyncState(existing.key, SyncState.WaitingForUpload)
    return updated
  }
}
