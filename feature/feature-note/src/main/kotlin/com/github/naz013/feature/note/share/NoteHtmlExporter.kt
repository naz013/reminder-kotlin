package com.github.naz013.feature.note.share

import com.github.naz013.domain.note.NoteDocument

/** Self-contained HTML document: `<h1>`-`<h3>` for headings, one `<ul>` per run of bullet lines, `<p>`
 * with `<br>` between consecutive plain lines (a blank line starts a new paragraph) and
 * `<strong>/<em>/<s>/<u>` plus a style `<span>` for the rest. All text is HTML-escaped.
 * [backgroundArgb]/[contentArgb] style the body when given. */
internal fun NoteDocument.toHtmlDocument(
  title: String,
  backgroundArgb: Int?,
  contentArgb: Int?,
  fontName: (Int) -> String?,
): String {
  val bodyStyle = listOfNotNull(
    backgroundArgb?.let { "background-color:${cssColor(it)}" },
    contentArgb?.let { "color:${cssColor(it)}" },
  ).joinToString(";")
  val bodyTag = if (bodyStyle.isEmpty()) "<body>" else "<body style=\"$bodyStyle\">"
  return buildString {
    appendLine("<!DOCTYPE html>")
    appendLine("<html>")
    appendLine("<head>")
    appendLine("<meta charset=\"UTF-8\">")
    appendLine("<title>${title.escapeHtml()}</title>")
    appendLine("</head>")
    appendLine(bodyTag)
    toHtmlBody(fontName).forEach { appendLine(it) }
    appendLine("</body>")
    append("</html>")
  }
}

internal fun NoteDocument.toHtmlBody(fontName: (Int) -> String?): List<String> {
  val out = mutableListOf<String>()
  val paragraph = mutableListOf<String>()
  var inList = false

  fun flushParagraph() {
    if (paragraph.isNotEmpty()) out += "<p>${paragraph.joinToString("<br>")}</p>"
    paragraph.clear()
  }

  fun closeList() {
    if (inList) out += "</ul>"
    inList = false
  }

  for (line in flatten(fontName)) {
    val content = line.runs.joinToString("") { it.toHtml() }
    if (line.kind != LineKind.BULLET) closeList()
    when (line.kind) {
      LineKind.PARAGRAPH -> if (line.isEmpty) flushParagraph() else paragraph += content
      LineKind.BULLET -> {
        flushParagraph()
        if (!inList) out += "<ul>"
        inList = true
        out += "<li>$content</li>"
      }

      else -> {
        flushParagraph()
        val level = line.kind.ordinal
        out += "<h$level>$content</h$level>"
      }
    }
  }
  flushParagraph()
  closeList()
  return out
}

private fun FlatRun.toHtml(): String {
  if (core.isEmpty() || style.isPlain) return text.escapeHtml()
  var html = core.escapeHtml()
  if (style.bold) html = "<strong>$html</strong>"
  if (style.italic) html = "<em>$html</em>"
  if (style.strikethrough) html = "<s>$html</s>"
  if (style.underline) html = "<u>$html</u>"
  style.css?.let { html = "<span style=\"$it\">$html</span>" }
  return leading.escapeHtml() + html + trailing.escapeHtml()
}

internal fun String.escapeHtml(): String = buildString(length) {
  for (char in this@escapeHtml) {
    when (char) {
      '&' -> append("&amp;")
      '<' -> append("&lt;")
      '>' -> append("&gt;")
      '"' -> append("&quot;")
      else -> append(char)
    }
  }
}
