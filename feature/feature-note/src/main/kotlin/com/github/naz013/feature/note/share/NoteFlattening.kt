package com.github.naz013.feature.note.share

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import java.util.Locale

internal enum class LineKind { PARAGRAPH, HEADING1, HEADING2, HEADING3, BULLET }

/** Inline formatting of a run. [colorCss], [sizePx] and [family] are folded into one [css] style. */
internal data class RunStyle(
  val bold: Boolean = false,
  val italic: Boolean = false,
  val underline: Boolean = false,
  val strikethrough: Boolean = false,
  val colorCss: String? = null,
  val sizePx: Int? = null,
  val family: String? = null,
) {
  val css: String?
    get() = listOfNotNull(
      colorCss,
      sizePx?.let { "font-size:${it}px" },
      family?.let { "font-family:'$it'" },
    ).takeIf { it.isNotEmpty() }?.joinToString(";")

  val isPlain: Boolean
    get() = !bold && !italic && !underline && !strikethrough && css == null
}

/** A same-styled slice of one line; the exporters only decorate [core], never the [leading]/[trailing] blanks. */
internal data class FlatRun(val text: String, val style: RunStyle) {
  val core: String = text.trim()
  val leading: String = if (core.isEmpty()) text else text.substring(0, text.length - text.trimStart().length)
  val trailing: String = if (core.isEmpty()) "" else text.substring(text.trimEnd().length)
}

internal data class FlatLine(val kind: LineKind, val runs: List<FlatRun>) {
  val isEmpty: Boolean get() = runs.isEmpty()
}

/** Resolves overlapping spans into per-line runs of uniform style. For each attribute family the
 * last span covering a character wins; color, size and family of a character end up in one style. */
internal fun NoteDocument.flatten(fontName: (Int) -> String?): List<FlatLine> {
  var lineStart = 0
  return text.split('\n').map { line ->
    val lineEnd = lineStart + line.length
    val flat = FlatLine(lineKind(lineStart, lineEnd, spans), runsOf(line, lineStart, spans, fontName))
    lineStart = lineEnd + 1
    flat
  }
}

private fun lineKind(lineStart: Int, lineEnd: Int, spans: List<NoteTextSpan>): LineKind {
  val attribute = spans.firstOrNull { span ->
    val touches = if (lineStart == lineEnd) span.start == lineStart else span.start < lineEnd && span.end > lineStart
    touches && span.attribute.toLineKind() != null
  }?.attribute
  return attribute?.toLineKind() ?: LineKind.PARAGRAPH
}

private fun NoteSpanAttribute.toLineKind(): LineKind? = when (this) {
  NoteSpanAttribute.Heading1 -> LineKind.HEADING1
  NoteSpanAttribute.Heading2 -> LineKind.HEADING2
  NoteSpanAttribute.Heading3 -> LineKind.HEADING3
  NoteSpanAttribute.BulletItem -> LineKind.BULLET
  else -> null
}

private fun runsOf(line: String, lineStart: Int, spans: List<NoteTextSpan>, fontName: (Int) -> String?): List<FlatRun> {
  if (line.isEmpty()) return emptyList()
  val styles = line.indices.map { styleAt(lineStart + it, spans, fontName) }
  val runs = mutableListOf<FlatRun>()
  var runStart = 0
  while (runStart < line.length) {
    var runEnd = runStart + 1
    while (runEnd < line.length && styles[runEnd] == styles[runStart]) runEnd++
    runs += FlatRun(line.substring(runStart, runEnd), styles[runStart])
    runStart = runEnd
  }
  return runs
}

private fun styleAt(position: Int, spans: List<NoteTextSpan>, fontName: (Int) -> String?): RunStyle {
  val covering = spans.filter { it.start <= position && position < it.end }.map { it.attribute }
  fun has(attribute: NoteSpanAttribute) = covering.any { it == attribute }
  val size = covering.lastOrNull { it is NoteSpanAttribute.FontSize } as? NoteSpanAttribute.FontSize
  val family = covering.lastOrNull { it is NoteSpanAttribute.FontFamily } as? NoteSpanAttribute.FontFamily
  val color = covering.lastOrNull { it is NoteSpanAttribute.SolidColor || it is NoteSpanAttribute.GradientColor }
  return RunStyle(
    bold = has(NoteSpanAttribute.Bold),
    italic = has(NoteSpanAttribute.Italic),
    underline = has(NoteSpanAttribute.Underline),
    strikethrough = has(NoteSpanAttribute.Strikethrough),
    colorCss = color?.let(::colorDeclarations),
    sizePx = size?.sp,
    family = family?.let { fontName(it.code) },
  )
}

private fun colorDeclarations(attribute: NoteSpanAttribute): String? = when (attribute) {
  is NoteSpanAttribute.SolidColor -> "color:${cssColor(attribute.argb)}"
  is NoteSpanAttribute.GradientColor -> {
    val stops = attribute.colors.joinToString(",") { cssColor(it) }
    val angle = attribute.angleDegrees.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }
    "background:linear-gradient(${angle}deg,$stops);-webkit-background-clip:text;color:transparent"
  }

  else -> null
}

/** `#RRGGBB` when opaque, otherwise `rgba(r,g,b,a)` with the alpha as a 0-1 fraction. */
internal fun cssColor(argb: Int): String {
  val alpha = (argb ushr 24) and 0xFF
  val red = (argb shr 16) and 0xFF
  val green = (argb shr 8) and 0xFF
  val blue = argb and 0xFF
  if (alpha == 0xFF) return String.format(Locale.ROOT, "#%02X%02X%02X", red, green, blue)
  val fraction = String.format(Locale.ROOT, "%.2f", alpha / 255f).trimEnd('0').trimEnd('.')
  return "rgba($red,$green,$blue,$fraction)"
}
