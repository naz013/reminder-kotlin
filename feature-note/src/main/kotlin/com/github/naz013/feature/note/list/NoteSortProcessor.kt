package com.github.naz013.feature.note.list

/**
 * Sort order values understood by [com.github.naz013.repository.NoteRepository.observeNotes]'s
 * SQL ORDER BY — the sort itself now happens in the DB query, not in Kotlin.
 */
object NoteSortProcessor {
  const val DATE_AZ = "date_az"
  const val DATE_ZA = "date_za"
  const val TEXT_AZ = "text_az"
  const val TEXT_ZA = "text_za"
}
