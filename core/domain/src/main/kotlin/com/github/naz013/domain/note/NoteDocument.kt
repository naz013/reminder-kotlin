package com.github.naz013.domain.note

import java.io.Serializable

private const val DISPLAY_TITLE_MAX_LENGTH = 120

/** The unified, formattable text content of a note - a plain [text] string plus a set of
 * character-range [spans] carrying inline (bold/italic/color/...) and line-level
 * (heading/bullet) formatting. There is no separate block/document structure - a heading or
 * bullet is just another span attribute applied to a whole line. */
data class NoteDocument(
  val text: String = "",
  val spans: List<NoteTextSpan> = emptyList(),
) : Serializable {

  companion object {
    /** Converts the pre-rich-text `title`/`summary` split into a single document, promoting a
     * non-blank [title] to a [NoteSpanAttribute.Heading1]-styled first line. */
    fun fromLegacy(title: String, summary: String): NoteDocument {
      if (title.isBlank()) return NoteDocument(text = summary)
      val text = if (summary.isBlank()) title else "$title\n$summary"
      return NoteDocument(
        text = text,
        spans = listOf(NoteTextSpan(0, title.length, NoteSpanAttribute.Heading1)),
      )
    }
  }
}

/** First line of [NoteDocument.text], truncated - used for list/search sort, notifications,
 * widgets, and share subjects. Deliberately doesn't inspect [NoteDocument.spans]: the natural
 * authoring pattern already puts a heading on the first line when a "title" is wanted. */
fun NoteDocument.displayTitle(): String {
  val firstLineEnd = text.indexOf('\n').let { if (it == -1) text.length else it }
  return text.substring(0, firstLineEnd).take(DISPLAY_TITLE_MAX_LENGTH)
}
