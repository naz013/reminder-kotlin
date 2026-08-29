package com.github.naz013.domain.note

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTextSpanSerializationTest {

  @Test
  fun `blank json deserializes to an empty span list`() {
    assertEquals(emptyList<NoteTextSpan>(), "".toNoteTextSpans())
  }

  @Test
  fun `round-trips a span for every attribute type`() {
    val spans = listOf(
      NoteTextSpan(0, 4, NoteSpanAttribute.Bold),
      NoteTextSpan(0, 4, NoteSpanAttribute.Italic),
      NoteTextSpan(0, 4, NoteSpanAttribute.Underline),
      NoteTextSpan(0, 4, NoteSpanAttribute.Strikethrough),
      NoteTextSpan(0, 4, NoteSpanAttribute.Heading1),
      NoteTextSpan(0, 4, NoteSpanAttribute.Heading2),
      NoteTextSpan(0, 4, NoteSpanAttribute.Heading3),
      NoteTextSpan(0, 4, NoteSpanAttribute.BulletItem),
      NoteTextSpan(4, 10, NoteSpanAttribute.FontFamily(code = 3)),
      NoteTextSpan(4, 10, NoteSpanAttribute.FontSize(sp = 18)),
      NoteTextSpan(4, 10, NoteSpanAttribute.SolidColor(argb = -0xff0000)),
      NoteTextSpan(4, 10, NoteSpanAttribute.GradientColor(colors = listOf(-0xff0000, -0xffff01), angleDegrees = 45f)),
    )

    val restored = spans.toJson().toNoteTextSpans()

    assertEquals(spans, restored)
  }
}
