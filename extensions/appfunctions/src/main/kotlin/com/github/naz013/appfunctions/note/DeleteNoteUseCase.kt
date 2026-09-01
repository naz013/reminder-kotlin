package com.github.naz013.appfunctions.note

import com.github.naz013.domain.note.Note
import com.github.naz013.repository.NoteRepository

/** Deliberately thin, mirroring this module's
 * [com.github.naz013.appfunctions.reminder.DeleteReminderUseCase] precedent - doesn't clean up
 * attached images/tags or schedule background delete-work the way feature-note's fuller
 * DeleteNoteUseCase does. This module doesn't depend on that one (see
 * extensions/appfunctions/build.gradle.kts) - an accepted trade-off, not an oversight. */
class DeleteNoteUseCase(
  private val noteRepository: NoteRepository,
) {
  suspend operator fun invoke(id: String): Note? {
    val existing = noteRepository.getById(id)?.note ?: return null
    noteRepository.delete(id)
    return existing
  }
}
