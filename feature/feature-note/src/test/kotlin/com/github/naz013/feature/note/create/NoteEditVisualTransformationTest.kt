package com.github.naz013.feature.note.create

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteEditVisualTransformationTest {

  @Test
  fun `no bullets leaves offsets unchanged`() {
    val document = NoteDocument(text = "Hello world")
    val transformed = noteEditVisualTransformation(document, 14) { null }.filter(
      androidx.compose.ui.text.AnnotatedString(document.text),
    )
    assertEquals("Hello world", transformed.text.text)
    assertEquals(5, transformed.offsetMapping.originalToTransformed(5))
    assertEquals(5, transformed.offsetMapping.transformedToOriginal(5))
  }

  @Test
  fun `bullet prefix shifts offsets after it and round-trips`() {
    // "Milk\nEggs" with "Eggs" as a bullet line
    val text = "Milk\nEggs"
    val document = NoteDocument(text = text, spans = listOf(NoteTextSpan(5, 9, NoteSpanAttribute.BulletItem)))
    val transformed = noteEditVisualTransformation(document, 14) { null }.filter(
      androidx.compose.ui.text.AnnotatedString(text),
    )
    assertEquals("Milk\n•  Eggs", transformed.text.text)

    // offsets before the bullet line are untouched
    assertEquals(0, transformed.offsetMapping.originalToTransformed(0))
    assertEquals(4, transformed.offsetMapping.originalToTransformed(4))
    // offset 5 (start of "Eggs") lands after the inserted "•  " prefix (length 3)
    assertEquals(8, transformed.offsetMapping.originalToTransformed(5))
    assertEquals(12, transformed.offsetMapping.originalToTransformed(9))

    // round-trip
    for (original in 0..text.length) {
      val forward = transformed.offsetMapping.originalToTransformed(original)
      assertEquals(original, transformed.offsetMapping.transformedToOriginal(forward))
    }

    // a transformed offset landing inside the inserted prefix snaps back to the line start
    assertEquals(5, transformed.offsetMapping.transformedToOriginal(6))
    assertEquals(5, transformed.offsetMapping.transformedToOriginal(7))
  }
}
