package com.github.naz013.usecase.notes

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository

class GetNoteByIdUseCase(
  private val noteRepository: NoteRepository
) {

  suspend operator fun invoke(id: String): NoteWithImages? {
    return noteRepository.getById(id)
  }
}
