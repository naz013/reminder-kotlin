package com.github.naz013.feature.note.share

import com.github.naz013.domain.note.NoteDocument

private val LeadingMarkerPattern = Regex("^(#{1,6}|[-+*]) ")
private const val HARD_BREAK = "  "
private const val MARKER_SEPARATOR = "<!---->"
private val MARKER_CHARS = setOf('*', '~')

/** Hybrid Markdown: headings, bullets, bold, italic and strikethrough are real Markdown; underline,
 * colors, gradients, font size and font family become inline HTML (`<u>`, `<span style>`) wrapped
 * outside the Markdown markers. Markers and tags are closed and reopened per line and never touch
 * the blanks around a run. Literal text is not escaped, except a backslash before a plain line that
 * would read as a heading or bullet; consecutive plain lines get a trailing hard break and a plain
 * line after a bullet is separated by a blank line so it does not become part of the item. */
internal fun NoteDocument.toHybridMarkdown(fontName: (Int) -> String?): String {
  val lines = flatten(fontName)
  val out = mutableListOf<String>()
  lines.forEachIndexed { index, line ->
    val next = lines.getOrNull(index + 1)
    var rendered = linePrefix(line.kind) + line.runs.map { it.toMarkdown() }.joinSeparatingMarkers()
    if (line.kind == LineKind.PARAGRAPH && LeadingMarkerPattern.containsMatchIn(rendered)) rendered = "\\$rendered"
    if (line.kind == LineKind.PARAGRAPH && !line.isEmpty && next?.kind == LineKind.PARAGRAPH && !next.isEmpty) {
      rendered += HARD_BREAK
    }
    out += rendered
    if (line.kind == LineKind.BULLET && next?.kind == LineKind.PARAGRAPH && !next.isEmpty) out += ""
  }
  return out.joinToString("\n")
}

/** Adjacent emphasis runs would fuse their `*`/`~` markers into one ambiguous delimiter run, so an HTML comment keeps them apart. */
private fun List<String>.joinSeparatingMarkers(): String = buildString {
  for (piece in this@joinSeparatingMarkers) {
    if (isNotEmpty() && last() in MARKER_CHARS && piece.firstOrNull() in MARKER_CHARS) append(MARKER_SEPARATOR)
    append(piece)
  }
}

private fun linePrefix(kind: LineKind): String = when (kind) {
  LineKind.HEADING1 -> "# "
  LineKind.HEADING2 -> "## "
  LineKind.HEADING3 -> "### "
  LineKind.BULLET -> "- "
  LineKind.PARAGRAPH -> ""
}

private fun FlatRun.toMarkdown(): String {
  if (core.isEmpty() || style.isPlain) return text
  val emphasis = when {
    style.bold && style.italic -> "***"
    style.bold -> "**"
    style.italic -> "*"
    else -> ""
  }
  val strike = if (style.strikethrough) "~~" else ""
  val spanOpen = style.css?.let { "<span style=\"$it\">" }.orEmpty()
  val spanClose = if (spanOpen.isEmpty()) "" else "</span>"
  val underlineOpen = if (style.underline) "<u>" else ""
  val underlineClose = if (style.underline) "</u>" else ""
  return leading + spanOpen + underlineOpen + strike + emphasis + core + emphasis + strike +
    underlineClose + spanClose + trailing
}
