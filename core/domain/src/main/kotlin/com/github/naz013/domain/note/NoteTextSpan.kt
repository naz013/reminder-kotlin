package com.github.naz013.domain.note

import java.io.Serializable

/** [start]/[end] are character offsets into the owning [NoteDocument.text], [end] exclusive. */
data class NoteTextSpan(
  val start: Int,
  val end: Int,
  val attribute: NoteSpanAttribute,
) : Serializable
