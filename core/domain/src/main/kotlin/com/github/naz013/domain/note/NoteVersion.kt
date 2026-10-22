package com.github.naz013.domain.note

/** A local-only snapshot of a note's [content] as it was before an edit overwrote it; [savedAt] is epoch millis. */
data class NoteVersion(
  val id: Long = 0L,
  val noteKey: String,
  val content: NoteDocument,
  val savedAt: Long,
)
