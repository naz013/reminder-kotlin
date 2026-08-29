package com.github.naz013.feature.note.create

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteSpanEditingTest {

  @Test
  fun `diffEdit finds insertion in the middle`() {
    val edit = diffEdit("Hello world", "Hello brave world")
    assertEquals(TextEdit(start = 6, oldEnd = 6, newEnd = 12), edit)
  }

  @Test
  fun `diffEdit finds deletion`() {
    val edit = diffEdit("Hello brave world", "Hello world")
    assertEquals(TextEdit(start = 6, oldEnd = 12, newEnd = 6), edit)
  }

  @Test
  fun `diffEdit finds full replacement of identical-length strings`() {
    val edit = diffEdit("cat", "dog")
    assertEquals(TextEdit(start = 0, oldEnd = 3, newEnd = 3), edit)
  }

  @Test
  fun `shiftSpans keeps a span entirely before the edit untouched`() {
    val spans = listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Bold))
    val result = shiftSpans("Hello world", "Hello brave world", spans)
    assertEquals(spans, result)
  }

  @Test
  fun `shiftSpans shifts a span entirely after an insertion`() {
    val spans = listOf(NoteTextSpan(6, 11, NoteSpanAttribute.Bold)) // "world"
    val result = shiftSpans("Hello world", "Hello brave world", spans)
    assertEquals(listOf(NoteTextSpan(12, 17, NoteSpanAttribute.Bold)), result)
  }

  @Test
  fun `shiftSpans extends a span when typing right after it`() {
    val spans = listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Bold)) // "Hello"
    val result = shiftSpans("Hello", "Hello!", spans)
    assertEquals(listOf(NoteTextSpan(0, 6, NoteSpanAttribute.Bold)), result)
  }

  @Test
  fun `shiftSpans keeps extending across multiple keystrokes typed right after styled text`() {
    val afterFirst = shiftSpans("Hello", "Hello!", listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Bold)))
    val afterSecond = shiftSpans("Hello!", "Hello!?", afterFirst)
    assertEquals(listOf(NoteTextSpan(0, 7, NoteSpanAttribute.Bold)), afterSecond)
  }

  @Test
  fun `shiftSpans extends every axis that ends at the insertion point`() {
    val spans = listOf(
      NoteTextSpan(0, 5, NoteSpanAttribute.Bold),
      NoteTextSpan(0, 5, NoteSpanAttribute.SolidColor(argb = -0xff0000)),
    )
    val result = shiftSpans("Hello", "Hello!", spans)
    assertEquals(
      listOf(
        NoteTextSpan(0, 6, NoteSpanAttribute.Bold),
        NoteTextSpan(0, 6, NoteSpanAttribute.SolidColor(argb = -0xff0000)),
      ),
      result,
    )
  }

  @Test
  fun `shiftSpans does not retroactively pull new text into a span typed right before it`() {
    val spans = listOf(NoteTextSpan(1, 5, NoteSpanAttribute.Bold)) // "ello" of "Hello"
    val result = shiftSpans("Hello", "HXello", spans)
    assertEquals(listOf(NoteTextSpan(2, 6, NoteSpanAttribute.Bold)), result)
  }

  @Test
  fun `shiftSpans drops a span entirely inside a deletion`() {
    val spans = listOf(NoteTextSpan(6, 11, NoteSpanAttribute.Bold)) // "brave"
    val result = shiftSpans("Hello brave world", "Hello  world", spans)
    assertEquals(emptyList<NoteTextSpan>(), result)
  }

  @Test
  fun `shiftSpans clips a span straddling the start of an edit`() {
    // span covers "lo bra" (3..9) in "Hello brave world"; deleting "brave" (6..11)
    val spans = listOf(NoteTextSpan(3, 9, NoteSpanAttribute.Bold))
    val result = shiftSpans("Hello brave world", "Hello  world", spans)
    assertEquals(listOf(NoteTextSpan(3, 6, NoteSpanAttribute.Bold)), result)
  }

  @Test
  fun `isAttributeActiveOverRange is true when every char in range is covered`() {
    val spans = listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Bold))
    assertTrue(isAttributeActiveOverRange(spans, NoteSpanAttribute.Bold, 1, 4))
  }

  @Test
  fun `isAttributeActiveOverRange is false when only part of the range is covered`() {
    val spans = listOf(NoteTextSpan(0, 3, NoteSpanAttribute.Bold))
    assertFalse(isAttributeActiveOverRange(spans, NoteSpanAttribute.Bold, 0, 5))
  }

  @Test
  fun `isAttributeActiveOverRange for a collapsed cursor checks the char before it`() {
    val spans = listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Bold))
    assertTrue(isAttributeActiveOverRange(spans, NoteSpanAttribute.Bold, 5, 5))
    assertFalse(isAttributeActiveOverRange(spans, NoteSpanAttribute.Bold, 0, 0))
  }

  @Test
  fun `applyAttribute clips an overlapping span of the same axis`() {
    val spans = listOf(NoteTextSpan(0, 10, NoteSpanAttribute.FontSize(14)))
    val result = applyAttribute(spans, 4, 6, NoteSpanAttribute.FontSize(20))
    assertEquals(
      listOf(
        NoteTextSpan(0, 4, NoteSpanAttribute.FontSize(14)),
        NoteTextSpan(4, 6, NoteSpanAttribute.FontSize(20)),
        NoteTextSpan(6, 10, NoteSpanAttribute.FontSize(14)),
      ),
      result,
    )
  }

  @Test
  fun `applyAttribute does not disturb a different axis`() {
    val spans = listOf(NoteTextSpan(0, 10, NoteSpanAttribute.Bold))
    val result = applyAttribute(spans, 4, 6, NoteSpanAttribute.Italic)
    assertTrue(result.contains(NoteTextSpan(0, 10, NoteSpanAttribute.Bold)))
    assertTrue(result.contains(NoteTextSpan(4, 6, NoteSpanAttribute.Italic)))
  }

  @Test
  fun `clearAxis removes only the requested axis`() {
    val spans = listOf(
      NoteTextSpan(0, 10, NoteSpanAttribute.Bold),
      NoteTextSpan(0, 10, NoteSpanAttribute.Italic),
    )
    val result = clearAxis(spans, NoteSpanAxis.BOLD, 2, 5)
    assertEquals(
      listOf(
        NoteTextSpan(0, 2, NoteSpanAttribute.Bold),
        NoteTextSpan(5, 10, NoteSpanAttribute.Bold),
        NoteTextSpan(0, 10, NoteSpanAttribute.Italic),
      ),
      result,
    )
  }

  @Test
  fun `currentLineRange finds the line around a cursor mid-document`() {
    val text = "Hello\nWorld\nAgain"
    assertEquals(0..4, currentLineRange(text, 2))
    assertEquals(6..10, currentLineRange(text, 6))
    assertEquals(6..10, currentLineRange(text, 11))
    assertEquals(12..16, currentLineRange(text, 12))
    assertEquals(12..16, currentLineRange(text, text.length))
  }

  @Test
  fun `activeLineFormat reads the span starting at the cursor's line`() {
    val text = "Title\nBody"
    val spans = listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Heading1))
    assertEquals(NoteSpanAttribute.Heading1, activeLineFormat(text, spans, 2))
    assertNull(activeLineFormat(text, spans, 7))
  }

  @Test
  fun `linesTouched returns every line overlapped by a multi-line selection`() {
    val text = "One\nTwo\nThree"
    val lines = linesTouched(text, 1, 6)
    assertEquals(listOf(0..2, 4..6), lines)
  }

  @Test
  fun `linesTouched for a collapsed cursor returns just that line`() {
    val text = "One\nTwo\nThree"
    assertEquals(listOf(4..6), linesTouched(text, 5, 5))
  }

  @Test
  fun `trimmedNoteDocument shifts spans past leading whitespace`() {
    val document = trimmedNoteDocument("  Hello world  ", listOf(NoteTextSpan(2, 7, NoteSpanAttribute.Bold)))
    assertEquals(NoteDocument("Hello world", listOf(NoteTextSpan(0, 5, NoteSpanAttribute.Bold))), document)
  }

  @Test
  fun `trimmedNoteDocument drops a span entirely inside trimmed whitespace`() {
    val document = trimmedNoteDocument("Hello   ", listOf(NoteTextSpan(6, 8, NoteSpanAttribute.Bold)))
    assertEquals(NoteDocument("Hello", emptyList()), document)
  }
}
