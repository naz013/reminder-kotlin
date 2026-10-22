package com.github.naz013.feature.note.preview

internal data class UiNoteVersion(
  val id: Long,
  val savedAtFormatted: String,
  val preview: String,
  val fullText: String,
)

/** [selectedVersion] `null` means the list is shown; otherwise its full text with a Restore button. */
internal data class NoteHistoryState(
  val versions: List<UiNoteVersion> = emptyList(),
  val selectedVersion: UiNoteVersion? = null,
)
