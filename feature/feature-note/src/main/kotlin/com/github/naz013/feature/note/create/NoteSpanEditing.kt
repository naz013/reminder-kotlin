package com.github.naz013.feature.note.create

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan

/** Which axis a [NoteSpanAttribute] belongs to - applying one attribute clips/removes any
 * existing span of the same axis on the affected range first, per [NoteSpanAttribute]'s own
 * axis grouping (bold/italic/underline/strikethrough are each their own axis and freely overlap
 * with everything else; font family and font size are independent axes from each other; solid
 * and gradient color share one axis since only one can apply to a range; the four line-level
 * attributes share one axis since a line is at most one of heading-1/2/3/bullet). */
internal enum class NoteSpanAxis { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, FONT_FAMILY, FONT_SIZE, COLOR, LINE_FORMAT }

internal fun NoteSpanAttribute.axis(): NoteSpanAxis = when (this) {
  NoteSpanAttribute.Bold -> NoteSpanAxis.BOLD
  NoteSpanAttribute.Italic -> NoteSpanAxis.ITALIC
  NoteSpanAttribute.Underline -> NoteSpanAxis.UNDERLINE
  NoteSpanAttribute.Strikethrough -> NoteSpanAxis.STRIKETHROUGH
  is NoteSpanAttribute.FontFamily -> NoteSpanAxis.FONT_FAMILY
  is NoteSpanAttribute.FontSize -> NoteSpanAxis.FONT_SIZE
  is NoteSpanAttribute.SolidColor, is NoteSpanAttribute.GradientColor -> NoteSpanAxis.COLOR
  NoteSpanAttribute.Heading1,
  NoteSpanAttribute.Heading2,
  NoteSpanAttribute.Heading3,
  NoteSpanAttribute.BulletItem,
  -> NoteSpanAxis.LINE_FORMAT
}

/** The [start, oldEnd) region of the old text that was replaced by the [start, newEnd) region of
 * the new text, found via a common-prefix/common-suffix scan - cost is proportional to the size
 * of the actual edit, not the length of the note, since IME edits are always localized. */
internal data class TextEdit(val start: Int, val oldEnd: Int, val newEnd: Int) {
  val delta: Int get() = (newEnd - start) - (oldEnd - start)
}

internal fun diffEdit(old: String, new: String): TextEdit {
  val maxPrefix = minOf(old.length, new.length)
  var prefix = 0
  while (prefix < maxPrefix && old[prefix] == new[prefix]) prefix++

  val maxSuffix = minOf(old.length, new.length) - prefix
  var suffix = 0
  while (suffix < maxSuffix && old[old.length - 1 - suffix] == new[new.length - 1 - suffix]) suffix++

  return TextEdit(start = prefix, oldEnd = old.length - suffix, newEnd = new.length - suffix)
}

/** Shifts/clips [spans] (offsets into [old]) so they stay correctly positioned in [new], given a
 * single edit region. A span entirely inside the edited-away region is dropped; a span straddling
 * the edit is clipped to whichever side survives. A span that ends exactly where new text was
 * inserted is extended to cover it - typing right after styled text (or right at the end of a
 * heading/bullet line) continues in that same style, matching the toolbar's own cursor-position
 * read (see [NoteEditState.activeFormat]) rather than dropping back to plain text. Typing right
 * *before* styled text does not retroactively pull the new text into it - only the trailing edge
 * is sticky. */
internal fun shiftSpans(old: String, new: String, spans: List<NoteTextSpan>): List<NoteTextSpan> {
  if (old == new) return spans
  val edit = diffEdit(old, new)
  val insertedLength = edit.newEnd - edit.start
  return spans.mapNotNull { span ->
    when {
      span.end < edit.start -> span
      span.end == edit.start -> if (insertedLength > 0) span.copy(end = span.end + insertedLength) else span
      span.start >= edit.oldEnd -> span.copy(start = span.start + edit.delta, end = span.end + edit.delta)
      else -> {
        val newStart = if (span.start <= edit.start) span.start else edit.newEnd
        val newEnd = if (span.end >= edit.oldEnd) span.end + edit.delta else edit.start
        if (newEnd <= newStart) null else span.copy(start = newStart, end = newEnd)
      }
    }
  }
}

/** True only if every character in `[start, end)` (or, for a collapsed cursor, the character
 * immediately before [start]) is covered by a span with this exact [attribute]. */
internal fun isAttributeActiveOverRange(spans: List<NoteTextSpan>, attribute: NoteSpanAttribute, start: Int, end: Int): Boolean {
  val rangeStart = if (end > start) start else (start - 1).coerceAtLeast(0)
  val rangeEnd = if (end > start) end else start
  if (rangeEnd <= rangeStart) return false
  var pos = rangeStart
  while (pos < rangeEnd) {
    val covering = spans.firstOrNull { it.attribute == attribute && it.start <= pos && pos < it.end } ?: return false
    pos = covering.end
  }
  return true
}

/** Returns the single line-format attribute (if any) covering the line containing [pos]. */
internal fun activeLineFormat(text: String, spans: List<NoteTextSpan>, pos: Int): NoteSpanAttribute? {
  val lineStart = currentLineRange(text, pos).first
  return spans.firstOrNull { it.attribute.axis() == NoteSpanAxis.LINE_FORMAT && it.start == lineStart }?.attribute
}

/** The solid text color (ARGB) covering `[start, end)` (or, for a collapsed cursor, the character
 * immediately before [start]) - null if any part of the range is uncolored or covered by more
 * than one distinct color, matching [isAttributeActiveOverRange]'s all-or-nothing semantics. */
internal fun activeSolidColorArgb(spans: List<NoteTextSpan>, start: Int, end: Int): Int? {
  val rangeStart = if (end > start) start else (start - 1).coerceAtLeast(0)
  val rangeEnd = if (end > start) end else start
  if (rangeEnd <= rangeStart) return null
  var pos = rangeStart
  var argb: Int? = null
  while (pos < rangeEnd) {
    val covering = spans.firstOrNull { it.attribute is NoteSpanAttribute.SolidColor && it.start <= pos && pos < it.end } ?: return null
    val color = (covering.attribute as NoteSpanAttribute.SolidColor).argb
    if (argb != null && argb != color) return null
    argb = color
    pos = covering.end
  }
  return argb
}

/** Applies [attribute] over `[start, end)`, first clipping/removing any existing span sharing its
 * [NoteSpanAxis] on that range (standard normalize-on-apply). */
internal fun applyAttribute(spans: List<NoteTextSpan>, start: Int, end: Int, attribute: NoteSpanAttribute): List<NoteTextSpan> {
  if (start >= end) return spans
  val cleared = clearAxis(spans, attribute.axis(), start, end)
  return (cleared + NoteTextSpan(start, end, attribute)).sortedBy { it.start }
}

/** Removes any span on [axis] overlapping `[start, end)`, clipping spans that only partially
 * overlap rather than deleting them outright. */
internal fun clearAxis(spans: List<NoteTextSpan>, axis: NoteSpanAxis, start: Int, end: Int): List<NoteTextSpan> {
  if (start >= end) return spans
  val result = mutableListOf<NoteTextSpan>()
  for (span in spans) {
    if (span.attribute.axis() != axis || span.end <= start || span.start >= end) {
      result.add(span)
      continue
    }
    if (span.start < start) result.add(span.copy(end = start))
    if (span.end > end) result.add(span.copy(start = end))
  }
  return result
}

/** The `[start, end)` range (newline-delimited) of the line containing [pos]. */
internal fun currentLineRange(text: String, pos: Int): IntRange {
  val clamped = pos.coerceIn(0, text.length)
  val newlineBefore = if (clamped == 0) -1 else text.lastIndexOf('\n', clamped - 1)
  val lineStart = newlineBefore + 1
  val newlineAt = text.indexOf('\n', clamped)
  val lineEnd = if (newlineAt == -1) text.length else newlineAt
  return lineStart until lineEnd
}

/** Trims leading/trailing whitespace from [text] before saving, shifting/clipping [spans] to
 * stay correctly positioned in the trimmed result rather than silently drifting. */
internal fun trimmedNoteDocument(text: String, spans: List<NoteTextSpan>): NoteDocument {
  val leadingTrim = text.indexOfFirst { !it.isWhitespace() }.let { if (it == -1) text.length else it }
  val trimmedText = text.trim()
  val adjustedSpans = spans.mapNotNull { span ->
    val newStart = (span.start - leadingTrim).coerceIn(0, trimmedText.length)
    val newEnd = (span.end - leadingTrim).coerceIn(0, trimmedText.length)
    if (newEnd <= newStart) null else span.copy(start = newStart, end = newEnd)
  }
  return NoteDocument(text = trimmedText, spans = adjustedSpans)
}

/** Every line range touched by the selection `[lo, hi]` (a single line if collapsed). */
internal fun linesTouched(text: String, lo: Int, hi: Int): List<IntRange> {
  if (lo == hi) return listOf(currentLineRange(text, lo))
  val result = mutableListOf<IntRange>()
  var pos = lo
  while (pos <= hi) {
    val range = currentLineRange(text, pos)
    result.add(range)
    val nextLineStart = range.last + 2
    if (nextLineStart > hi || nextLineStart > text.length) break
    pos = nextLineStart
  }
  return result
}
