package com.elementary.tasks.notes.list

data class NotesScreenState(
  val listState: ListState = ListState.Loading,
  val isGrid: Boolean = false,
  val searchQuery: String = "",
  val sortOrder: String = NoteSortProcessor.DATE_ZA,
  val isArchived: Boolean = false,
)

sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val notes: List<UiNoteListItem>,
  ) : ListState

  data object Empty : ListState
}

enum class NoteMenuAction {
  OPEN,
  EDIT,
  SHARE,
  SHOW_IN_STATUS_BAR,
  ARCHIVE,
  UNARCHIVE,
  DELETE,
}
