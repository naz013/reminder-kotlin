package com.github.naz013.ui.note

/**
 * The three ways the notes list can render its items. Persisted (by name) via
 * [NotePreferences.notesLayoutMode] so the last-used layout is restored on the next visit.
 */
enum class ListLayoutMode {
  LIST,
  GRID,
  STAGGERED_GRID,
}
