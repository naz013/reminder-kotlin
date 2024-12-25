package com.github.naz013.usecase.notes

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository

class SearchNotesByTextUseCase(
  private val noteRepository: NoteRepository
) {

  suspend operator fun invoke(query: String, isArchived: Boolean = false): List<NoteWithImages> {
    return noteRepository.searchByText(query = query, isArchived = isArchived)
  }
}
