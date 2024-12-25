package com.github.naz013.usecase.notes

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository

class GetAllNotesUseCase(
  private val noteRepository: NoteRepository
) {

  suspend operator fun invoke(isArchived: Boolean = false): List<NoteWithImages> {
    return noteRepository.getAll(isArchived = isArchived)
  }
}
