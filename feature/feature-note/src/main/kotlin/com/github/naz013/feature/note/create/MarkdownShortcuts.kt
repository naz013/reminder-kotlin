package com.github.naz013.feature.note.create

import com.github.naz013.domain.note.NoteSpanAttribute

/** A line-start Markdown marker the user just completed by typing a space: the `[markerStart,
 * markerEnd)` range of the new text (marker plus the typed space) is to be removed and
 * [attribute] applied to the line. */
internal data class MarkdownShortcut(
  val markerStart: Int,
  val markerEnd: Int,
  val attribute: NoteSpanAttribute,
)

private val ShortcutMarkers = mapOf(
  "#" to NoteSpanAttribute.Heading1,
  "##" to NoteSpanAttribute.Heading2,
  "###" to NoteSpanAttribute.Heading3,
  "-" to NoteSpanAttribute.BulletItem,
  "*" to NoteSpanAttribute.BulletItem,
)

/** Detects a single space typed at [caret] in [new] (exactly one character added to [old]) that
 * completes a `#`/`##`/`###`/`-`/`*` marker at the start of its line. Anything else - pastes,
 * restores, deletions, markers not at line start - returns null. */
internal fun detectMarkdownShortcut(old: String, new: String, caret: Int): MarkdownShortcut? {
  if (new.length != old.length + 1 || caret < 2 || caret > new.length) return null
  val spaceIndex = caret - 1
  if (new[spaceIndex] != ' ' || new.removeRange(spaceIndex, caret) != old) return null
  val lineStart = new.lastIndexOf('\n', spaceIndex - 1) + 1
  val attribute = ShortcutMarkers[new.substring(lineStart, spaceIndex)] ?: return null
  return MarkdownShortcut(markerStart = lineStart, markerEnd = caret, attribute = attribute)
}
