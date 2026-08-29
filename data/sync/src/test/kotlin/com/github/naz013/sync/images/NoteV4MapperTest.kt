package com.github.naz013.sync.images

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.files.model.NoteV4Span
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteV4MapperTest {

  @Test
  fun `round-trips every span attribute type through the V4 wire shape`() {
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
    val document = NoteDocument(text = "Shopping list", spans = spans)

    val restored = document.toV4Spans().toNoteTextSpans()

    assertEquals(spans, restored)
  }

  @Test
  fun `unknown span type is dropped rather than crashing`() {
    val spans = listOf(NoteV4Span(start = 0, end = 4, type = "SomeFutureType"))

    val restored = spans.toNoteTextSpans()

    assertEquals(emptyList<NoteTextSpan>(), restored)
  }
}
