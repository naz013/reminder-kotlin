package com.github.naz013.domain.note

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteDocumentTest {

  @Test
  fun `fromLegacy returns plain document when title is blank`() {
    val document = NoteDocument.fromLegacy(title = "", summary = "Buy milk")

    assertEquals("Buy milk", document.text)
    assertEquals(emptyList<NoteTextSpan>(), document.spans)
  }

  @Test
  fun `fromLegacy promotes non-blank title to a heading-styled first line`() {
    val document = NoteDocument.fromLegacy(title = "Shopping", summary = "Buy milk")

    assertEquals("Shopping\nBuy milk", document.text)
    assertEquals(
      listOf(NoteTextSpan(0, "Shopping".length, NoteSpanAttribute.Heading1)),
      document.spans,
    )
  }

  @Test
  fun `fromLegacy with blank summary does not add a trailing newline`() {
    val document = NoteDocument.fromLegacy(title = "Shopping", summary = "")

    assertEquals("Shopping", document.text)
    assertEquals(
      listOf(NoteTextSpan(0, "Shopping".length, NoteSpanAttribute.Heading1)),
      document.spans,
    )
  }

  @Test
  fun `displayTitle returns the whole text when there is a single line`() {
    val document = NoteDocument(text = "Buy milk and eggs")

    assertEquals("Buy milk and eggs", document.displayTitle())
  }

  @Test
  fun `displayTitle returns only the first line of a multi-line document`() {
    val document = NoteDocument(text = "Shopping\nBuy milk\nBuy eggs")

    assertEquals("Shopping", document.displayTitle())
  }

  @Test
  fun `displayTitle truncates a very long first line`() {
    val longLine = "a".repeat(200)
    val document = NoteDocument(text = "$longLine\nmore text")

    assertEquals(120, document.displayTitle().length)
    assertEquals("a".repeat(120), document.displayTitle())
  }

  @Test
  fun `displayTitle is blank for an empty document`() {
    assertEquals("", NoteDocument().displayTitle())
  }
}
