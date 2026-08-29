package com.github.naz013.sync.images

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.files.model.NoteV4Span

internal fun NoteDocument.toV4Spans(): List<NoteV4Span> = spans.map { it.toV4Span() }

internal fun List<NoteV4Span>.toNoteTextSpans(): List<NoteTextSpan> = mapNotNull { it.toDomain() }

private fun NoteTextSpan.toV4Span(): NoteV4Span = when (val attribute = attribute) {
  NoteSpanAttribute.Bold -> NoteV4Span(start = start, end = end, type = "Bold")
  NoteSpanAttribute.Italic -> NoteV4Span(start = start, end = end, type = "Italic")
  NoteSpanAttribute.Underline -> NoteV4Span(start = start, end = end, type = "Underline")
  NoteSpanAttribute.Strikethrough -> NoteV4Span(start = start, end = end, type = "Strikethrough")
  NoteSpanAttribute.Heading1 -> NoteV4Span(start = start, end = end, type = "Heading1")
  NoteSpanAttribute.Heading2 -> NoteV4Span(start = start, end = end, type = "Heading2")
  NoteSpanAttribute.Heading3 -> NoteV4Span(start = start, end = end, type = "Heading3")
  NoteSpanAttribute.BulletItem -> NoteV4Span(start = start, end = end, type = "BulletItem")
  is NoteSpanAttribute.FontFamily -> NoteV4Span(start = start, end = end, type = "FontFamily", intValue = attribute.code)
  is NoteSpanAttribute.FontSize -> NoteV4Span(start = start, end = end, type = "FontSize", intValue = attribute.sp)
  is NoteSpanAttribute.SolidColor -> NoteV4Span(start = start, end = end, type = "SolidColor", intValue = attribute.argb)
  is NoteSpanAttribute.GradientColor -> NoteV4Span(
    start = start,
    end = end,
    type = "GradientColor",
    colors = attribute.colors,
    angleDegrees = attribute.angleDegrees,
  )
}

private fun NoteV4Span.toDomain(): NoteTextSpan? {
  val attribute = when (type) {
    "Bold" -> NoteSpanAttribute.Bold
    "Italic" -> NoteSpanAttribute.Italic
    "Underline" -> NoteSpanAttribute.Underline
    "Strikethrough" -> NoteSpanAttribute.Strikethrough
    "Heading1" -> NoteSpanAttribute.Heading1
    "Heading2" -> NoteSpanAttribute.Heading2
    "Heading3" -> NoteSpanAttribute.Heading3
    "BulletItem" -> NoteSpanAttribute.BulletItem
    "FontFamily" -> intValue?.let { NoteSpanAttribute.FontFamily(code = it) }
    "FontSize" -> intValue?.let { NoteSpanAttribute.FontSize(sp = it) }
    "SolidColor" -> intValue?.let { NoteSpanAttribute.SolidColor(argb = it) }
    "GradientColor" -> colors?.let { NoteSpanAttribute.GradientColor(colors = it, angleDegrees = angleDegrees ?: 0f) }
    else -> null
  } ?: return null
  return NoteTextSpan(start = start, end = end, attribute = attribute)
}
