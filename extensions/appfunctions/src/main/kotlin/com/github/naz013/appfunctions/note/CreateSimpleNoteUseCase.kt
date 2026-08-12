package com.github.naz013.appfunctions.note

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.NoteRepository

class CreateSimpleNoteUseCase(
  private val noteRepository: NoteRepository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    title: String,
    content: String,
  ): Note {
    val note =
      Note(syncState = SyncState.WaitingForUpload).apply {
        this.title = title
        this.summary = content
        this.date = dateTimeManager.getNowGmtDateTime()
      }
    noteRepository.save(note)
    return note
  }
}
