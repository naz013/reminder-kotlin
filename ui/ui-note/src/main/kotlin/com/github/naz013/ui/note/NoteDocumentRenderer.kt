package com.github.naz013.ui.note

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import kotlin.math.roundToInt

private const val HEADING_1_SCALE = 1.6f
private const val HEADING_2_SCALE = 1.4f
private const val HEADING_3_SCALE = 1.2f
private const val BULLET_PREFIX = "•  "

/**
 * Renders [NoteDocument] as one styled [AnnotatedString] for read-only display (list/grid cards,
 * previews) - the note-content counterpart to the note-card background styling already handled
 * elsewhere. Bullet lines get a literal [BULLET_PREFIX] inserted into the text: safe here since
 * nothing is editable, unlike the interactive editor which decorates bullets without touching the
 * buffer (see its own renderer) so cursor/selection offsets stay untouched.
 *
 * [baseFontSizeSp] is the note's own default body size (`Note.fontSize`); heading spans scale
 * relative to it. [fontFamilyResolver] maps a [NoteSpanAttribute.FontFamily] code to a Compose
 * [FontFamily], typically backed by [NoteFontProvider]. [maxChars], when set, truncates [text]
 * (and drops/clips spans past that point) before building the result - for a card that only ever
 * shows a handful of lines behind `TextOverflow.Ellipsis`, this keeps a note with a very long,
 * heavily-spanned body from making every list item pay for the whole document on each render.
 */
fun NoteDocument.toAnnotatedString(
  baseFontSizeSp: Int,
  maxChars: Int? = null,
  fontFamilyResolver: (Int) -> FontFamily? = { null },
): AnnotatedString {
  if (text.isEmpty()) return AnnotatedString("")

  val truncatedText = if (maxChars != null && text.length > maxChars) text.substring(0, maxChars) else text
  val truncatedSpans = if (truncatedText.length == text.length) {
    spans
  } else {
    spans.mapNotNull { span ->
      if (span.start >= truncatedText.length) null else span.copy(end = minOf(span.end, truncatedText.length))
    }
  }

  val lineStarts = listOf(0) + truncatedText.indices.filter { truncatedText[it] == '\n' }.map { it + 1 }
  val bulletLineStarts = truncatedSpans
    .filter { it.attribute == NoteSpanAttribute.BulletItem }
    .map { it.start }
    .filter { it in lineStarts }
    .sorted()

  val displayText = StringBuilder()
  var previous = 0
  for (position in bulletLineStarts) {
    displayText.append(truncatedText, previous, position)
    displayText.append(BULLET_PREFIX)
    previous = position
  }
  displayText.append(truncatedText, previous, truncatedText.length)

  fun mapOffset(original: Int): Int {
    val insertedBefore = bulletLineStarts.count { it <= original }
    return original + insertedBefore * BULLET_PREFIX.length
  }

  return buildAnnotatedString {
    append(displayText.toString())
    truncatedSpans.forEach { span ->
      val spanStyle = span.attribute.toSpanStyle(baseFontSizeSp, fontFamilyResolver) ?: return@forEach
      addStyle(spanStyle, mapOffset(span.start), mapOffset(span.end))
    }
  }
}

private fun NoteSpanAttribute.toSpanStyle(
  baseFontSizeSp: Int,
  fontFamilyResolver: (Int) -> FontFamily?,
): SpanStyle? = when (this) {
  NoteSpanAttribute.Bold -> SpanStyle(fontWeight = FontWeight.Bold)
  NoteSpanAttribute.Italic -> SpanStyle(fontStyle = FontStyle.Italic)
  NoteSpanAttribute.Underline -> SpanStyle(textDecoration = TextDecoration.Underline)
  NoteSpanAttribute.Strikethrough -> SpanStyle(textDecoration = TextDecoration.LineThrough)
  NoteSpanAttribute.BulletItem -> null
  NoteSpanAttribute.Heading1 -> headingStyle(baseFontSizeSp, HEADING_1_SCALE)
  NoteSpanAttribute.Heading2 -> headingStyle(baseFontSizeSp, HEADING_2_SCALE)
  NoteSpanAttribute.Heading3 -> headingStyle(baseFontSizeSp, HEADING_3_SCALE)
  is NoteSpanAttribute.FontFamily -> fontFamilyResolver(code)?.let { SpanStyle(fontFamily = it) }
  is NoteSpanAttribute.FontSize -> SpanStyle(fontSize = sp.sp)
  is NoteSpanAttribute.SolidColor -> SpanStyle(color = Color(argb))
  is NoteSpanAttribute.GradientColor -> SpanStyle(brush = Brush.linearGradient(colors.map { Color(it) }))
}

private fun headingStyle(baseFontSizeSp: Int, scale: Float): SpanStyle =
  SpanStyle(fontWeight = FontWeight.Bold, fontSize = (baseFontSizeSp * scale).roundToInt().sp)
