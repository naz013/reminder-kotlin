package com.github.naz013.ui.note

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteDocumentRendererTest {

  @Test
  fun `toAnnotatedString keeps full text when under maxChars`() {
    val document = NoteDocument(text = "Hello world", spans = listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Bold)))

    val result = document.toAnnotatedString(baseFontSizeSp = 14, maxChars = 500)

    assertEquals("Hello world", result.text)
  }

  @Test
  fun `toAnnotatedString truncates text past maxChars`() {
    val document = NoteDocument(text = "a".repeat(1000))

    val result = document.toAnnotatedString(baseFontSizeSp = 14, maxChars = 500)

    assertEquals(500, result.text.length)
  }

  @Test
  fun `toAnnotatedString drops a span entirely past maxChars`() {
    val document = NoteDocument(
      text = "a".repeat(1000),
      spans = listOf(NoteTextSpan(600, 610, NoteSpanAttribute.Bold)),
    )

    val result = document.toAnnotatedString(baseFontSizeSp = 14, maxChars = 500)

    assertEquals(emptyList<AnnotatedStringRange>(), result.spanStyles.map { AnnotatedStringRange(it.start, it.end) })
  }

  @Test
  fun `toAnnotatedString clips a span straddling maxChars`() {
    val document = NoteDocument(
      text = "a".repeat(1000),
      spans = listOf(NoteTextSpan(490, 510, NoteSpanAttribute.Bold)),
    )

    val result = document.toAnnotatedString(baseFontSizeSp = 14, maxChars = 500)

    assertEquals(listOf(AnnotatedStringRange(490, 500)), result.spanStyles.map { AnnotatedStringRange(it.start, it.end) })
  }

  data class AnnotatedStringRange(val start: Int, val end: Int)
}
