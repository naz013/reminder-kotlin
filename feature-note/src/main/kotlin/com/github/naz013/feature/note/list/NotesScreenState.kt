package com.github.naz013.feature.note.list

import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.tag.TagChipState

internal data class NotesScreenState(
  val listState: ListState = ListState.Loading,
  val isGrid: Boolean = false,
  val searchQuery: String = "",
  val sortOrder: String = NoteSortProcessor.DATE_ZA,
  val isArchived: Boolean = false,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagId: String? = null,
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
